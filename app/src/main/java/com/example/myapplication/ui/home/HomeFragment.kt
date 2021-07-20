package com.example.myapplication.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MainListAdapter
import com.example.myapplication.MapsActivity
import com.example.myapplication.R
import com.example.myapplication.database.DataEntity
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    private lateinit var listView: ListView
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

        listView = binding.listViewMain
        buttonOne = binding.mainButton1
        buttonTwo = binding.mainButton2
        buttonThree = binding.mainButton3

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

        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
        val marketPref = sharedPref.getString("market","")
        val pharmacyPref = sharedPref.getString("pharmacy","")
        val gasPref = sharedPref.getString("gastation","")
        val hospitalPref = sharedPref.getString("hospital","")

        sharedPref.edit().remove("market").apply()
        sharedPref.edit().remove("pharmacy").apply()
        sharedPref.edit().remove("gastation").apply()
        sharedPref.edit().remove("hospital").apply()

        if (marketPref != null) {
            shoppingList[0] = marketPref
        }
        if(pharmacyPref != null) {
            shoppingList[1] = pharmacyPref
        }
        if (gasPref != null) {
            shoppingList[2] = gasPref
        }
        if(hospitalPref != null) {
            shoppingList[3] = hospitalPref
        }

        //inizialmente i pulsati vengono disabilitati e nascosti, per poi apparire con un'animazione di transizione quando viene premuto quello centrale
        buttonTwo.alpha = 0f
        buttonThree.alpha = 0f
        buttonTwo.isEnabled = false
        buttonThree.isEnabled = false

        buttonOne.setOnClickListener {
            buttonOne.alpha = 0f
            buttonOne.isEnabled = false

            buttonTwo.animate().alpha(1f).translationXBy(-200F).duration = 2000
            buttonThree.animate().alpha(1f).translationXBy(200F).duration = 2000
            buttonTwo.isEnabled = true
            buttonThree.isEnabled = true
        }

        //inizializzazione della listView e dell'adapter
        list.add("Supermercato")
        list.add("Farmacia")
        list.add("Benzinaio")
        list.add("Ospedale")
        val adapter = MainListAdapter(this.context, R.layout.list_item_main, list)

        listView.adapter = adapter

        //registrazione di un listener per visualizzare un alertDialog dove poter inserire la lista della spesa per poi salvarla nelle preferenze
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            onItemClick(shoppingList, list, position)
        }

        //registrazione di un listener per visualizzare un alertDialog contenente gli elementi inseriti nell'elemento selezionato della RecyclerView
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            onItemLongClick(shoppingList,list,position)
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
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(shopping_list: MutableList<String>) {

        val sList = shopping_list[0]
        val pList = shopping_list[1]
        val gList = shopping_list[2]
        val hList = shopping_list[3]
        

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        max_id++
        val entity: DataEntity =
            com.example.myapplication.database.DataEntity(max_id, Date().time / 1000,currentDate, sList, pList, gList, hList)
        mViewModel.viewModelScope.launch {
            mViewModel.insert(entity)
        }

    }

    private fun onItemClick(shopping_list: MutableList<String>,
                            list: MutableList<String>, position: Int) {
        val alertDialog = AlertDialog.Builder(this.context).create()
        val dialogView = layoutInflater.inflate(R.layout.service_dialog, null)
        alertDialog.setView(dialogView)
        val editText = dialogView.rootView.findViewById<EditText>(R.id.edittext_dialog)
        when (list[position]) {
            "Supermercato" -> {
                alertDialog.setTitle("Lista supermercato")
                alertDialog.setIcon(R.drawable.ic_market)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per il supermercato..."
                if (shopping_list[0] != "") editText.setText(shopping_list[0])
            }
            "Farmacia" -> {
                alertDialog.setTitle("Lista farmacia")
                alertDialog.setIcon(R.drawable.ic_pharma)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per il farmacia..."
                if (shopping_list[1] != "") editText.setText(shopping_list[1])
            }

            "Benzinaio" -> {
                alertDialog.setTitle("Lista benzinaio")
                alertDialog.setIcon(R.drawable.ic_gpl)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per il benzinaio..."
                if (shopping_list[2] != "") editText.setText(shopping_list[2])
            }
            "Ospedale" -> {
                alertDialog.setTitle("Lista ospedale")
                alertDialog.setIcon(R.drawable.ic_hospital)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per l'ospedale..."
                if (shopping_list[3] != "") editText.setText(shopping_list[3])
            }
        }
        //setto il pulsante positivo OK in modo che mi salvi le cose scritte nella lista all'interno
        //dell'array shopping_list, in base a quale pulsante è stato cliccato avrà un indice diverso nell'array
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
            val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                context?.applicationContext
            )
            val editor = sharedPref.edit()
            when (list[position]) {
                "Supermercato" ->{ shopping_list[0] = editText.text.toString()
                    editor.putString("market",editText.text.toString()).apply() }
                "Farmacia" -> { shopping_list[1] = editText.text.toString()
                    editor.putString("pharmacy",editText.text.toString()).apply() }
                "Benzinaio" ->{ shopping_list[2] = editText.text.toString()
                    editor.putString("gastation",editText.text.toString()).apply() }
                "Ospedale" -> { shopping_list[3] = editText.text.toString()
                    editor.putString("hospital",editText.text.toString()).apply() }
            }
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            "Cancel"
        ) { dialog, which -> alertDialog.dismiss() }
        alertDialog.show()
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
}