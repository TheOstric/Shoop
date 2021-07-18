package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.model.nearbyplaces.PlacesModel
import com.example.myapplication.remote.Common
import com.example.myapplication.remote.GoogleAPI
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.internal.ContextUtils.getActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor


class MapsActivity : FragmentActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{
    private var mMap: GoogleMap? = null

    companion object{
        const val PERMISSION_CODE : Int = 1000
    }

    private var currentLat: Double = 0.0
    private var currentLong:Double = 0.0

    private lateinit var lastLocation : Location
    private var marker: Marker? = null
    private var polyline: Polyline? = null

    //Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var mService: GoogleAPI
    private lateinit var mScalarService: GoogleAPI
    internal lateinit var currentPlacesModel: PlacesModel
    private lateinit var mExecutor: Executor

    val pointsList = ArrayList<Double>()
    val driving = ArrayList<String>()

    lateinit var checkedItems: BooleanArray

    val blockingQueue = ArrayBlockingQueue<ArrayList<Double>>(1)

    val pBlockingQueue = ArrayBlockingQueue<PolylineOptions>(1)

    val list: HashMap<String,String> = HashMap()

    //variable used to determine whenever a distance choosed is not enough to discover the nearest service required
    var error = false

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val market = intent.extras?.getString("market")
        val pharmacy = intent.extras?.getString("pharmacy")

        if (market != "") {
            list["supermarket"] = market!!
        } else {
            list["supermarket"] = ""
        }

        if (pharmacy != "") {
            list["pharmacy"] = pharmacy!!
        } else {
            list["pharmacy"] = ""
        }
        //Initialize service
        mService = Common.googleAPI
        mScalarService = Common.googleAPIScalars

        mExecutor = ContextCompat.getMainExecutor(this)

        val driveButton = findViewById<Button>(R.id.driving_button)
        val listButton = findViewById<Button>(R.id.shoppinglist_button)
        //Request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                Log.d("TAG","QUI")
                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                )


            }
        } else {
            Log.d("TAG","QUI1")
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()!!
            )
        }
        //mMap?.clear()




        //nearbyPlace(list)

        driveButton.setOnClickListener {
            if (pointsList.size > 2){
                Log.d("POINTS",pointsList.toString())
                val URL = getDrivingUrl()
                val location: Uri = Uri.parse(URL)
                val mapIntent = Intent(Intent.ACTION_VIEW, location)
                startActivity(mapIntent)
            } else {
                Toast.makeText(this,"Non sono stati inseriti elementi nelle liste, quindi nessuna destinazione è stata individuata.",Toast.LENGTH_SHORT).show()
            }
        }

        listButton.setOnClickListener {

            // Set up the alert builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Lista della spesa")

            if (market == "" && pharmacy == "") {
                builder.setMessage("La lista è vuota. Si prega di inserire elementi nella lista della spesa.")
            } else {
                // Add a checkbox list
                if (market != null && pharmacy != null) {
                    val shopList = market.lines().plus(pharmacy.lines())

                    val array: Array<String> = shopList.toTypedArray()

                    checkedItems = load(array.size)
                    builder.setMultiChoiceItems(array, checkedItems) { dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                    }
                } else {
                    builder.setMessage("Errore! Provare a re-inserire la lista della spesa.")
                }
            }


            // Add OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                if (checkedItems.isNotEmpty())
                    save(checkedItems)
            }
            builder.setNegativeButton("Cancel", null)

// Create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }
    }
    private fun nearbyPlace (typePlace: HashMap<String,String>){

        //Remove all markers on the map
       // mMap?.clear()
        var posLat: Double
        var posLong: Double
        var srcLat = currentLat
        var srcLng = currentLong
        pointsList.add(srcLat)
        pointsList.add(srcLng)
        var arrayList: ArrayList<Double>
        var error = false
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Errori!")

        Log.d("SIZE",typePlace.size.toString())
        for (j in typePlace.keys) {
            if (typePlace[j] != ""){
                Log.e("HERE","qui")
                //build URL request base on location
                val url = getUrl(srcLat, srcLng, j)
                val destinations: MutableList<String> = ArrayList()
                val names: MutableList<String> = ArrayList()
                val latLngs: MutableList<LatLng> = ArrayList()


                mService.getNearbyPlaces(url)
                    .enqueue(object : Callback<PlacesModel> {
                        @RequiresApi(Build.VERSION_CODES.P)
                        override fun onResponse(
                            call: Call<PlacesModel>,
                            response: Response<PlacesModel>
                        ) {
                            currentPlacesModel = response.body()!!

                            if (response.isSuccessful) {

                                if (!response.body()!!.results!!.isEmpty()) {
                                    for (i in response.body()!!.results!!.indices) {
                                        val googlePlace = response.body()!!.results!![i]
                                        val lat = googlePlace.geometry!!.location!!.lat
                                        val long = googlePlace.geometry!!.location!!.lng
                                        val placeName = googlePlace.name
                                        //latLng = LatLng(lat, long)

                                        val dest = StringBuilder(lat.toString()).append("%2C")
                                            .append(long.toString())

                                        destinations.add(dest.toString())
                                        if (placeName != null) {
                                            names.add(placeName)
                                        }
                                        latLngs.add(LatLng(lat, long))
                                    }
                                } else {
                                    error = true
                                }

                                if (!error) {
                                    val thread = Thread {
                                        getDestination(destinations, names, latLngs, srcLat, srcLng)
                                    }
                                    thread.start()

                                    arrayList = blockingQueue.take()
                                    posLat = arrayList[0]
                                    posLong = arrayList[1]

                                    val markerOptions = MarkerOptions()
                                    val destlatlong = LatLng(posLat, posLong)
                                    markerOptions.position(destlatlong)
                                    markerOptions.title(names[latLngs.indexOf(destlatlong)])
                                    markerOptions.icon(
                                        com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                                        )
                                    )
                                    //Add marker to map
                                    mMap!!.addMarker(markerOptions)
                                    //Move camera to adapt view
                                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(destlatlong))
                                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(17f))

                                    val thread1 = Thread {
                                        drawPath(srcLat, srcLng, posLat, posLong)
                                    }
                                    thread1.start()


                                    val poly = pBlockingQueue.take()
                                    mMap!!.addPolyline(poly)

                                    srcLat = posLat
                                    srcLng = posLong

                                    pointsList.add(srcLat)
                                    pointsList.add(srcLng)
                                    Log.e("LISTIS",pointsList.toString())
                                    driving.add("${srcLat},${srcLng}")
                                } else {
                                    builder.setMessage(j + ": non ci sono negozi di questo tipo nell'area della distanza selezionata." +
                                            " Si prega di selezionare un'altra distanza e ricalcolare l'itinerario.")
                                }
                            }
                        }
                        override fun onFailure(call: Call<PlacesModel>, t: Throwable) {
                            Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        if (error)
            builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawPath(startingLat: Double, startingLong: Double, destLat: Double, destLng: Double) {
        //polyline?.remove()

        val url = getDirectionUrl(startingLat,startingLong,destLat,destLng)
        val response = mService.getDirections(url).execute()
        val route = response.body()!!.routes?.get(0)?.legs?.get(0)?.steps
        val points = ArrayList<LatLng>()
        val path = ArrayList<List<LatLng>>()
        for (i in route!!.indices){
            val point = route[i].end_location
            val startPoint = route[i].start_location

            val startlat = startPoint!!.lat
            val startLong = startPoint.lng

            val endlat = point!!.lat
            val endlong = point.lng
            points.add(LatLng(startlat,startLong))
            points.add(LatLng(endlat,endlong))
        }
        path.add(points)
        val polyLineOptions = PolylineOptions()
        for (i in path.indices){
            polyLineOptions.addAll(path[i])
                .width(12f)
                .color(Color.GREEN)
                .geodesic(true)
        }
        pBlockingQueue.add(polyLineOptions)
    }

    private fun getDestination(destinations: MutableList<String>, names: MutableList<String>?, latLngs: MutableList<LatLng>, startingLat: Double, startingLong: Double){

       // mMap?.clear()

        val url = getDistanceUrl(startingLat,startingLong, destinations)
        lateinit var latlng: LatLng
        var duration: Int? = null

        val insideRes = ArrayList<Double>()
        val response = mService.getDuration(url).execute()
        Log.d("Lista",response.toString())
        if (response.body()!!.rows!!.isNotEmpty()) {

            for (i in response.body()!!.rows!!.indices) {
                val dist = response.body()!!.rows!![i]

                for (j in dist.elements!!.indices) {
                    if (duration == null) {
                        duration = dist.elements!![j].duration?.value
                        latlng = latLngs[j]
                    } else {
                        if (duration > dist.elements!![j].duration?.value!!) {
                            duration = dist.elements!![j].duration?.value
                            latlng = latLngs[j]
                        }
                    }
                }
            }

            insideRes.add(latlng.latitude)
            insideRes.add((latlng.longitude))

            blockingQueue.add(insideRes)
        } else {
            latlng = LatLng(0.0,0.0)
        }
    }

    private fun getUrl(currentLat: Double, currentLong: Double, typePlace: String): String {
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var radius = sharedPref.getString("radius","empty")

        if (radius == "500m")
            radius = "500"
        if (radius == "1km")
            radius = "1000"
        if (radius == "5km")
            radius = "5000"
        if (radius == "10km")
            radius = "10000"
        if (radius == "empty")
            radius = "1000"
        val googleURL = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googleURL.append("?location=$currentLat,$currentLong")
        googleURL.append("&radius=$radius")
        googleURL.append("&type=$typePlace")
        googleURL.append("&key=")

        Log.d("URLCHECK",googleURL.toString())
        return googleURL.toString()
    }

    private fun getDrivingUrl(): String {

        val googleURL = StringBuilder("https://www.google.com/maps/dir/?api=1")
        googleURL.append("&travelmode=driving")
        googleURL.append("&dir_action=navigate")
        googleURL.append("&origin=${pointsList[0]},${pointsList[1]}")
        googleURL.append("&destination=${pointsList[pointsList.size-2]},${pointsList[pointsList.size-1]}")
        if( pointsList.size > 4) {
            googleURL.append("&waypoints=${driving[0]}")
            for (i in driving.indices - 1) {
                if (i != 0)
                    googleURL.append("|${driving[i]}")
            }
        }

        Log.d("CHECK DRIVING",googleURL.toString())
        return googleURL.toString()
    }

    private fun getDirectionUrl(currentLat: Double, currentLong: Double, destLat: Double, destLng: Double): String {
        val googleURL = StringBuilder("https://maps.googleapis.com/maps/api/directions/json")
        googleURL.append("?origin=$currentLat,$currentLong")
        googleURL.append("&destination=$destLat,$destLng")
        googleURL.append("&key=")

        return googleURL.toString()
    }

    private fun getDistanceUrl(currentLat: Double, currentLong: Double, destCoordinates: MutableList<String>): String {
        val googleURL = StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json")
        googleURL.append("?units=imperial")
        googleURL.append("&origins=$currentLat,$currentLong")
        googleURL.append("&destinations=")
        for (i in destCoordinates.indices){
            val currentItem: String = destCoordinates[i]
            if ( i != destCoordinates.size-1 )
                googleURL.append("$currentItem%7C")
            else
                googleURL.append(currentItem)
        }
        googleURL.append("&key=")

        return googleURL.toString()
    }

    private fun buildLocationCallback(){
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?){
                lastLocation = p0!!.lastLocation

                if (marker != null)
                    marker!!.remove()

                currentLat = lastLocation.latitude
                currentLong = lastLocation.longitude

                val latLng = LatLng(currentLat,currentLong)
                val markerOptions = MarkerOptions().position(latLng).title("Your position").icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN))

                marker = mMap!!.addMarker(markerOptions)

                mMap!!.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.zoomTo(11f))
                nearbyPlace(list)

            }
        }
    }

    private fun buildLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this@MapsActivity)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                             // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                this@MapsActivity,
                                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                            )
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        })
    }

    private fun checkLocationPermission(): Boolean{
        if(androidx.core.content.ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, kotlin.arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, kotlin.arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),PERMISSION_CODE)
            return false
        }
        else
            return true
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.uiSettings.isZoomControlsEnabled = true


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                mMap!!.isMyLocationEnabled = true
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMyLocationButtonClick(): Boolean {
       return true
    }

    override fun onMyLocationClick(p0: Location) {
        TODO("Not yet implemented")
    }

    private fun save(isChecked: BooleanArray) {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (i in isChecked.indices) {
            editor.putBoolean(i.toString(), isChecked[i])
        }
        editor.apply()
    }

    private fun load(size: Int): BooleanArray {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val reChecked = BooleanArray(size)
        for (i in 0 until size) {
            reChecked[i] = sharedPreferences.getBoolean(i.toString(), false)
        }
        return reChecked
    }
}