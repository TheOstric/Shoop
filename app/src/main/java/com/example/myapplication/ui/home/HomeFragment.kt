package com.example.myapplication.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MapsActivity
import com.example.myapplication.R
import com.example.myapplication.database.DataEntity
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.database.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment(R.layout.fragment_home), CoroutineScope by MainScope(){

   // private lateinit var homeViewModel: HomeViewModel
    private var fragmentHomeBinding: FragmentHomeBinding? = null

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //variabili usate per usare la listView e pulsanti presente nel fragment
    //private lateinit var listView: ListView
    private lateinit  var buttonOne: Button
    private lateinit var buttonTwo: Button
    private lateinit  var buttonThree: Button

    //variabile usata per
    private lateinit var mViewModel : ViewModel

    //rappresentazione dell'ID massimo usato finora nel database, così da poterne usare sempre nuovi e non creare collisioni
    private var max_id = 0

    //lista di stringhe contenente i nomi che devono essere visualizzati sulla listView
    private val list: MutableList<String> = LinkedList<String>()

    //lista di stringhe usata per contenere gli elementi che sono stati scritti sulle varie liste
    private val shoppingList : MutableList<String> = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private var itemTouchHelper: ItemTouchHelper? = null
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //inizializzazione del layout del fragment e delle sue componenti
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerViewMain
        linearLayoutManager = LinearLayoutManager(this.context)
        recyclerView.layoutManager = linearLayoutManager
        sharedPref = PreferenceManager.getDefaultSharedPreferences(
            context?.applicationContext
        )

        val services = ArrayList<Service>()
        services.add(Service(R.drawable.ic_market,"Supermercato",R.drawable.radius))
        services.add(Service(R.drawable.ic_pharma,"Farmacia",R.drawable.radius_teal))
        services.add(Service(R.drawable.ic_gpl,"Benzinaio",R.drawable.radius_gas))
        services.add(Service(R.drawable.ic_hospital,"Ospedale",R.drawable.radius_hosp))

        list.add("supermarket")
        list.add("pharmacy")
        list.add("gas_station")
        list.add("hospital")

        //listView = binding.listViewMain
        buttonOne = binding.mainButton1
        buttonTwo = binding.mainButton2
        buttonThree = binding.mainButton3

        val myAdapter =
            this.context?.let { RecyclerViewAdapter(services,shoppingList,list, it, layoutInflater) }
        recyclerView.adapter = myAdapter
        val callback: ReorderHelper? = myAdapter?.let { context?.let { it1 ->
            ReorderHelper(it, shoppingList, list,
                it1, layoutInflater  )
        } }
        itemTouchHelper = callback?.let { ItemTouchHelper(it) }
        itemTouchHelper?.attachToRecyclerView(recyclerView)

        //inizializzazione del ModelView usato come interfaccia per interagire con il database
        mViewModel = activity?.let { ViewModelProvider(it) }?.get(ViewModel::class.java)!!
        //inizializzazione della variabile max_id con il massimo valore di id usato finora per una entry nel database
        mViewModel.viewModelScope.launch {
            val i: Int? = mViewModel.getMaxId()
            if (i == null)
                max_id = 0
            else
                max_id = i
        }

        //inizializzazione della shoppingList con valori vuoti
        //per poi andare ad aggiungere eventuali valori salvati nelle preferenze, che rappresentano:
        // -> precedenti valori inseriti nelle liste, salvati per poter essere preservati qualora si cambiasse fragment o activity
        // -> risultato dell'utilizzo del pulsante "copia" usato per copiare la lista di una spesa precedente, salvata nel database
        shoppingList.add("")
        shoppingList.add("")
        shoppingList.add("")
        shoppingList.add("")

        if (savedInstanceState != null) {
            shoppingList[list.indexOf("supermarket")] =
                savedInstanceState.getString("supermarket", "")
            shoppingList[list.indexOf("pharmacy")] =
                savedInstanceState.getString("pharmacy", "")
            shoppingList[list.indexOf("gas_station")] =
                savedInstanceState.getString("gas_station", "")
            shoppingList[list.indexOf("hospital")] =
                savedInstanceState.getString("hospital", "")
        } else {
            shoppingList[list.indexOf("supermarket")] = sharedPref.getString("market","").toString()
            shoppingList[list.indexOf("pharmacy")] = sharedPref.getString("pharmacy","").toString()
            shoppingList[list.indexOf("gas_station")] = sharedPref.getString("gas_station","").toString()
            shoppingList[list.indexOf("hospital")] = sharedPref.getString("hospital","").toString()

            val editor = sharedPref.edit()
            editor.remove("market")
            editor.remove("pharmacy")
            editor.remove("gas_station")
            editor.remove("hospital")
        }



        //inizialmente i pulsati vengono disabilitati e nascosti, per poi apparire con un'animazione di transizione quando viene premuto quello centrale
        buttonTwo.alpha = 0f
        buttonThree.alpha = 0f
        buttonTwo.isEnabled = false
        buttonThree.isEnabled = false

        buttonOne.setOnClickListener {
            buttonOne.alpha = 0f
            buttonOne.isEnabled = false

            buttonTwo.animate().alpha(1f).translationXBy(-150F).duration = 2000
            buttonThree.animate().alpha(1f).translationXBy(150F).duration = 2000
            buttonTwo.isEnabled = true
            buttonThree.isEnabled = true
        }

        //registrazione di un listener per salvare i dati inseriti nel database
        buttonTwo.setOnClickListener {
            saveData(shoppingList)
        }

        //registrazione di un listener per passare all'activity con cui calcolare il percorso più breve per i vari servizi scelti
        buttonThree.setOnClickListener {
            calculateTrip(shoppingList)
        }
    }

    private fun calculateTrip(shoppingList: MutableList<String>) {
        val intent = Intent(this.context, MapsActivity::class.java)
        intent.putExtra("market", shoppingList[0])
        intent.putExtra("pharmacy",shoppingList[1])
        intent.putExtra("gas", shoppingList[2])
        intent.putExtra("hospital",shoppingList[3])
        intent.putExtra("0", list[0])
        intent.putExtra("1", list[1])
        intent.putExtra("2", list[2])
        intent.putExtra("3", list[3])
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(shopping_list: MutableList<String>) {
        var exc = false
        val sList = shopping_list[0]
        val pList = shopping_list[1]
        val gList = shopping_list[2]
        val hList = shopping_list[3]

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        max_id++
        val entity: DataEntity =
            com.example.myapplication.database.DataEntity(max_id, Date().time / 1000,currentDate, sList, pList, gList, hList)
        try {
            mViewModel.viewModelScope.launch {
                mViewModel.insert(entity)
            }
        } catch (e: Exception){
            exc = true
        }

        if (!exc) {
            Toast.makeText(this.context, "Salvato.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onItemLongClick(shopping_list: MutableList<String>,
                                list: MutableList<String>, position: Int) : Boolean{
        val alertDialog = AlertDialog.Builder(this.context).create()
        val dialogView = layoutInflater.inflate(R.layout.visualize_service_dialog, null)
        alertDialog.setView(dialogView)
        val textView = dialogView.rootView.findViewById<TextView>(R.id.visualize_textview)
        alertDialog.setCancelable(true)
        when (list[position]) {
            "Supermercato" -> if (shopping_list[0] != "") textView.text =
                shopping_list[0] else textView.setText(R.string.Vuoto)
            "Farmacia" -> if (shopping_list[1] != "") textView.text =
                shopping_list[1] else textView.setText(R.string.Vuoto)
            "Benzinaio" -> if (shopping_list[2] != "") textView.text =
                shopping_list[2] else textView.setText(R.string.Vuoto)
            "Ospedale" -> if (shopping_list[3] != "") textView.text =
                shopping_list[3] else textView.setText(R.string.Vuoto)
        }
        alertDialog.show()
        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {

        val editor = sharedPref.edit()
        editor.remove("market")
        editor.remove("pharmacy")
        editor.remove("gas_station")
        editor.remove("hospital")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("supermarket",shoppingList[list.indexOf("supermarket")])
        outState.putString("pharmacy",shoppingList[list.indexOf("pharmacy")])
        outState.putString("gas_station",shoppingList[list.indexOf("gas_station")])
        outState.putString("hospital",shoppingList[list.indexOf("hospital")])
    }
}