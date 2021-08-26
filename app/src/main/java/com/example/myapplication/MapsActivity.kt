package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
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
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantLock


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

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var mService: GoogleAPI
    private lateinit var mScalarService: GoogleAPI
    internal lateinit var currentPlacesModel: PlacesModel

    val pointsList = ArrayList<String>()
    val driving = ArrayList<String>()

    lateinit var checkedItems: BooleanArray

    val blockingQueue = ArrayBlockingQueue<ArrayList<Double>>(1)

    val pBlockingQueue = ArrayBlockingQueue<PolylineOptions>(1)

    val list: ArrayList<String> = arrayListOf("","","","")

    lateinit var errorButton: Button

    private var nearbySearched: Boolean = false

    private var durations: LinkedHashMap<String,String> = LinkedHashMap()

    private var listaPos: ArrayList<String> = ArrayList()

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    //variable used to determine whenever a distance choosed is not enough to discover the nearest service required
    var error = ""

    private var count = 0

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        nearbySearched = false
        val market = intent.extras?.getString("market")
        val pharmacy = intent.extras?.getString("pharmacy")
        val gas = intent.extras?.getString("gas")
        val hospital = intent.extras?.getString("hospital")

        listaPos.add(intent.extras?.getString("0")!!)
        listaPos.add(intent.extras?.getString("1")!!)
        listaPos.add(intent.extras?.getString("2")!!)
        listaPos.add(intent.extras?.getString("3")!!)

        if (market != "") {
            list[listaPos.indexOf("supermarket")] = market!!
            count += 1
        } else {
            list[listaPos.indexOf("supermarket")] = ""
        }

        if (pharmacy != "") {
            list[listaPos.indexOf("pharmacy")] = pharmacy!!
            count += 1
        } else {
            list[listaPos.indexOf("pharmacy")] = ""
        }

        if (gas != "") {
            list[listaPos.indexOf("gas_station")] = gas!!
            count += 1
        } else {
            list[listaPos.indexOf("gas_station")] = ""
        }

        if (hospital != "") {
            list[listaPos.indexOf("hospital")] = hospital!!
            count += 1
        } else {
            list[listaPos.indexOf("hospital")] = ""
        }

        //Initialize service
        mService = Common.googleAPI
        mScalarService = Common.googleAPIScalars

        val driveButton = findViewById<Button>(R.id.driving_button)
        val listButton = findViewById<Button>(R.id.shoppinglist_button)
        errorButton = findViewById(R.id.error_button)

        //Request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
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
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()!!
            )
        }

        driveButton.setOnClickListener {
            if (pointsList.size > 0){
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

            if (market == "" && pharmacy == "" && gas == "" && hospital == "") {
                builder.setMessage("La lista è vuota. Si prega di inserire elementi nella lista della spesa.")
                checkedItems = load(0)
            } else {
                // Add a checkbox list
                val shopList = "SUPERMERCATO".lines().plus(market.lines()).plus("\nFARMACIA\n").plus(pharmacy.lines()).plus("\nBENZINAIO\n").plus(gas.lines()).plus("\nOSPEDALE\n").plus(hospital.lines())

                val array: Array<String> = shopList.toTypedArray()

                checkedItems = load(array.size)
                builder.setMultiChoiceItems(array, checkedItems) { dialog, which, isChecked ->
                    checkedItems[which] = isChecked
                }
            }


            // Add OK and Cancel buttons
            builder.setPositiveButton("Ok") { dialog, which ->
                if (checkedItems.isNotEmpty())
                    save(checkedItems)
            }
            builder.setNegativeButton("Cancel", null)

// Create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }

        errorButton.setOnClickListener {
            val popupMenu = PopupMenu(this,errorButton)
            popupMenu.menuInflater.inflate(R.menu.popup,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.info -> showDurations()
                    R.id.errors -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Errori!")
                        if(error != "")
                            builder.setMessage(error)
                        else
                            builder.setMessage("Non si sono verificati errori durante la ricerca.")

                        builder.setPositiveButton("Ok", null)

                        val dialog = builder.create()
                        dialog.show()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun nearbyPlace (typePlace: ArrayList<String>){

        //Remove all markers on the map
        // mMap?.clear()
        var posLat: Double
        var posLong: Double
        var srcLat = currentLat
        var srcLng = currentLong
        var arrayList: ArrayList<Double>
        var e = false
        var c = 0

        val t1 = Thread{
            for (j in listaPos.indices) {

                if (typePlace[j] != ""){

                    //build URL request base on location
                    val url = getUrl(srcLat, srcLng, listaPos[j])
                    val destinations: MutableList<String> = ArrayList()
                    val names: MutableList<String> = ArrayList()
                    val latLngs: MutableList<LatLng> = ArrayList()

                    val t = Thread {

                        val response = mService.getNearbyPlaces(url).execute()


                        currentPlacesModel = response.body()!!

                        if (response.isSuccessful) {

                            if (response.body()!!.results!!.isNotEmpty()) {
                                for (i in response.body()!!.results!!.indices) {
                                    val googlePlace = response.body()!!.results!![i]

                                    if (googlePlace.opening_hours?.open_now == true) {
                                        val lat = googlePlace.geometry!!.location!!.lat
                                        val long = googlePlace.geometry!!.location!!.lng
                                        val placeName = googlePlace.name


                                        val dest = StringBuilder(lat.toString()).append("%2C")
                                            .append(long.toString())

                                        destinations.add(dest.toString())
                                        if (placeName != null) {
                                            names.add(placeName)
                                        }
                                        latLngs.add(LatLng(lat, long))
                                    }
                                }
                                if (destinations.isEmpty()) {
                                    e = true
                                    error += listaPos[j] + ": tutti i negozi di questo tipo, entro la distanza selezionata, sono chiusi."
                                }
                            } else {
                                e = true
                                error += listaPos[j] + ": non ci sono negozi di questo tipo nell'area della distanza selezionata.\n" +
                                        " Si prega di selezionare un'altra distanza e ricalcolare l'itinerario.\n"
                            }


                            if (!e) {
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

                                this@MapsActivity.runOnUiThread{
                                    mMap!!.addMarker(markerOptions)
                                    //Move camera to adapt view
                                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(destlatlong))
                                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(17f))
                                }
                                //Add marker to map


                                val thread1 = Thread {
                                    drawPath(srcLat, srcLng, posLat, posLong)
                                }
                                thread1.start()


                                val poly = pBlockingQueue.take()
                                this@MapsActivity.runOnUiThread {
                                    mMap!!.addPolyline(poly)
                                }

                                srcLat = posLat
                                srcLng = posLong


                                driving.add("${srcLat},${srcLng}")
                                c += 1
                                if (c == count) {
                                    runOnUiThread{
                                        showDurations() }
                                }

                            } else {
                                c += 1
                                e = false
                            }

                        }
                        lock.lock()
                        condition.signal()
                        lock.unlock()
                    }

                    t.start()

                    lock.lock()
                    condition.await()
                    lock.unlock()
                }
            }
        }
        t1.start()
    }

    private fun showDurations() {
        val builder = AlertDialog.Builder(this@MapsActivity)
        builder.setTitle("Durate:")
        var min = 0
        var h = 0
        var message = ""
        for (i in durations.keys){
            message += i + ": " + durations[i] + "\n"
            val splitted = durations[i]?.split(" ")
            if (splitted != null) {
                if (splitted.size == 2){
                    min += splitted[0].toInt()
                }
                if (splitted.size == 4){
                    h += splitted[0].toInt()
                    min += splitted[2].toInt()
                }
            }
        }

        message += "\nDurata complessiva:\n"
        message += "$h h $min mins"
        builder.setMessage(message)

        builder.setPositiveButton("Ok",null)

        val dialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawPath(startingLat: Double, startingLong: Double, destLat: Double, destLng: Double) {

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

    private fun getDestination(destinations: MutableList<String>, names: MutableList<String>, latLngs: MutableList<LatLng>, startingLat: Double, startingLong: Double){

        val url = getDistanceUrl(startingLat,startingLong, destinations)

        lateinit var latlng: LatLng
        var duration: Int? = null
        var durationString: String? = null
        var destName = ""
        val destAddresses: ArrayList<String>?
        var pos = 0

        val insideRes = ArrayList<Double>()
        val response = mService.getDuration(url).execute()
        if (response.body()!!.rows!!.isNotEmpty()) {
            destAddresses = response.body()!!.destination_addresses as ArrayList<String>?
            for (i in response.body()!!.rows!!.indices) {
                val dist = response.body()!!.rows!![i]

                for (j in dist.elements!!.indices) {
                    if (duration == null) {
                        pos = j
                        duration = dist.elements!![j].duration?.value
                        durationString = dist.elements!![j].duration?.text
                        latlng = latLngs[j]
                        destName = names[j]
                    } else {
                        if (duration > dist.elements!![j].duration?.value!!) {
                            pos = j
                            duration = dist.elements!![j].duration?.value
                            durationString = dist.elements!![j].duration?.text
                            destName = names[j]
                            latlng = latLngs[j]
                        }
                    }
                }
            }

            insideRes.add(latlng.latitude)
            insideRes.add(latlng.longitude)

            durations[destName] = durationString!!

            val destAddr = destAddresses?.get(pos)?.split(",")
            var refactAddr = ""
            if (destAddr != null) {
                for (i in destAddr.indices) {
                    if( destAddr[i] != "") {
                        val part = destAddr[i].split(" ")
                        if (part[0] != "")
                            refactAddr += part[0]
                        else
                            refactAddr += part[1]
                        for (j in part.indices) {
                            if (part[0] != "") {
                                if (j != 0)
                                    refactAddr += "+" + part[j]
                            }
                            else {
                                if (j != 0 && j != 1)
                                    refactAddr += "+" + part[j]
                            }
                        }
                        if (i != destAddr.size-1)
                            refactAddr += ","
                    }
                }
            }

            pointsList.add(refactAddr)
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
        googleURL.append("&key=AIzaSyAEoHSbl8EBnnzKRIMqz7usULWu0c2DaSs")

        return googleURL.toString()
    }

    private fun getDrivingUrl(): String {

        val googleURL = StringBuilder("https://www.google.com/maps/dir/?api=1")
        googleURL.append("&travelmode=driving")
        googleURL.append("&dir_action=navigate")
        googleURL.append("&destination=${pointsList[pointsList.size-1]}")
        googleURL.append("&waypoints=")
        for (i in 0..pointsList.size-3)
            googleURL.append("${pointsList[i]}|")

        googleURL.append(pointsList[pointsList.size-2])

        return googleURL.toString()
    }

    private fun getDirectionUrl(currentLat: Double, currentLong: Double, destLat: Double, destLng: Double): String {

        val googleURL = StringBuilder("https://maps.googleapis.com/maps/api/directions/json")
        googleURL.append("?origin=$currentLat,$currentLong")
        googleURL.append("&destination=$destLat,$destLng")
        googleURL.append("&mode=driving")
        googleURL.append("&key=AIzaSyAEoHSbl8EBnnzKRIMqz7usULWu0c2DaSs")

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
        googleURL.append("&mode=driving")
        googleURL.append("&key=AIzaSyAEoHSbl8EBnnzKRIMqz7usULWu0c2DaSs")
        return googleURL.toString()
    }

    private fun buildLocationCallback(){
        locationCallback = object : LocationCallback(){
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onLocationResult(p0: LocationResult?){
                lastLocation = p0!!.lastLocation

                if (marker != null)
                    marker!!.remove()

                currentLat = lastLocation.latitude
                currentLong = lastLocation.longitude

                val latLng = LatLng(currentLat,currentLong)
                val markerOptions = MarkerOptions().position(latLng).title("Your position").icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE))

                marker = mMap!!.addMarker(markerOptions)
                mMap!!.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.zoomTo(11f))

                if (!nearbySearched) {

                    nearbyPlace(list)
                    nearbySearched = true
                }

            }
        }
    }

    private fun buildLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.numUpdates = 1
        locationRequest.interval = 0
    }

    private fun checkLocationPermission(): Boolean{
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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

    override fun onMyLocationClick(p0: Location) {
        TODO("Not yet implemented")
    }
}