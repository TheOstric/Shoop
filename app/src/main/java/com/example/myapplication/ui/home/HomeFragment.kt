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
import com.example.myapplication.Communicator
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

    private lateinit var listView: ListView
    private lateinit  var buttonOne: Button
    private lateinit var buttonTwo: Button
    private lateinit  var buttonThree: Button

    private lateinit var comm: Communicator
    private lateinit var mViewModel : ViewModel
    private var max_id = 0
    private val list: MutableList<String> = LinkedList<String>()
    private val shoppingList : MutableList<String> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        listView = binding.listViewMain
        buttonOne = binding.mainButton1
        buttonTwo = binding.mainButton2
        buttonThree = binding.mainButton3

        mViewModel = activity?.let { ViewModelProvider(it) }?.get(ViewModel::class.java)!!


        mViewModel.viewModelScope.launch {
            val i: Int? = mViewModel.getMaxId()
            if (i == null)
                max_id = 0
            else
                max_id = i
        }

        comm = requireActivity() as Communicator

        shoppingList.add("")
        shoppingList.add("")
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
        val marketPref = sharedPref.getString("market","")
        val pharmacyPref = sharedPref.getString("pharmacy","")
        sharedPref.edit().remove("market").apply()
        sharedPref.edit().remove("pharmacy").apply()
        if (marketPref != null) {
            shoppingList[0] = marketPref
        }
        if(pharmacyPref != null) {
            shoppingList[1] = pharmacyPref
        }

        buttonTwo.alpha = 0f
        buttonThree.alpha = 0f

        buttonOne.setOnClickListener {
            buttonOne.alpha = 0f
            buttonOne.isClickable = false

            buttonTwo.animate().alpha(1f).translationXBy(-200F).duration = 2000
            buttonThree.animate().alpha(1f).translationXBy(200F).duration = 2000
        }

        list.add("Supermercato")
        list.add("Farmacia")
        val adapter = MainListAdapter(this.context, R.layout.list_item_main, list)

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            onItemClick(shoppingList, list, position)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            onItemLongClick(shoppingList,list,position)
        }

        buttonTwo.setOnClickListener {
            saveData(shoppingList)
        }

        buttonThree.setOnClickListener {
            calculateTrip(shoppingList)
        }

        return root
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shoppingList.add("")
        shoppingList.add("")
        buttonTwo.alpha = 0f
        buttonThree.alpha = 0f

        buttonOne.setOnClickListener {
            buttonOne.alpha = 0f
            buttonOne.isClickable = false

            buttonTwo.animate().alpha(1f).translationXBy(-200F).duration = 2000
            buttonThree.animate().alpha(1f).translationXBy(200F).duration = 2000
        }

        list.add("Supermercato")
        list.add("Farmacia")
        val adapter = MainListAdapter(this.context, R.layout.list_item_main, list)

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            onItemClick(shoppingList, list, position)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            onItemLongClick(shoppingList,list,position)
        }

        buttonTwo.setOnClickListener {
            saveData(shoppingList)
        }

        buttonThree.setOnClickListener {
            calculateTrip(shoppingList)
        }
    }*/

    private fun calculateTrip(shoppingList: MutableList<String>) {
        val intent = Intent(this.context, MapsActivity::class.java)
        intent.putExtra("market", shoppingList[0])
        intent.putExtra("pharmacy",shoppingList[1])
        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("supermarket",shoppingList[0])
        outState.putString("pharmacy",shoppingList[1])
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(shopping_list: MutableList<String>) {

        val thread = Thread {
            val sList = shopping_list[0]
            val pList = shopping_list[1]

            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            max_id++
            val entity: DataEntity =
                com.example.myapplication.database.DataEntity(max_id, Date().time / 1000,currentDate, sList, pList)
            mViewModel.insert(entity)

        }

        thread.start()

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
                alertDialog.setIcon(R.drawable.supermarket)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per il supermercato..."
                if (shopping_list[0] != "") editText.setText(shopping_list[0])
            }
            "Farmacia" -> {
                alertDialog.setTitle("Lista farmacia")
                alertDialog.setIcon(R.drawable.red_cross)
                alertDialog.setCancelable(true)
                editText.hint = "Inserire la lista per il farmacia..."
                if (shopping_list[1] != "") editText.setText(shopping_list[1])
            }
        }
        //setto il pulsante positivo OK in modo che mi salvi le cose scritte nella lista all'interno
        //dell'array shopping_list, in base a quale pulsante è stato cliccato avrà un indice diverso nell'array
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
            when (list[position]) {
                "Supermercato" -> shopping_list[0] = editText.text.toString()
                "Farmacia" -> shopping_list[1] = editText.text.toString()
            }
            Toast.makeText(this.context,shopping_list[0],Toast.LENGTH_LONG).show()
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
        }
        alertDialog.show()
        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}