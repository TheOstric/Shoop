package com.example.myapplication.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSettingsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsFragment: Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var spinner: Spinner
    private lateinit var timeSpinner: Spinner
    private lateinit var travelSpinner: Spinner


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        spinner= binding.distancesSpinner
        timeSpinner = binding.timeSpinner

        val infoButtonOne = binding.fab
        TooltipCompat.setTooltipText(infoButtonOne, "Distanza massima entro cui vengono cercati i servizi selezionati.");

        val infoButtonTwo = binding.infoDb
        TooltipCompat.setTooltipText(infoButtonTwo, "Permanenza massima degli elementi nel database.");

        ArrayAdapter.createFromResource(
                this.requireContext(),
                R.array.distances_array,
                android.R.layout.simple_spinner_item
            ).also { adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.time_array,
            android.R.layout.simple_spinner_item
        ).also { adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            timeSpinner.adapter = adapter
        }

        val chosedItem = getPersistedItem()
        if ( chosedItem[0] != -1 ) {
            spinner.setSelection(chosedItem[0])
        } else {
            spinner.setSelection(0)
        }
        if ( chosedItem[1] != -1 ) {
            timeSpinner.setSelection(chosedItem[1])
        } else {
            timeSpinner.setSelection(0)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setPersistedItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setPersistedTimeItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPersistedItem(): ArrayList<Int>{
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        val array = ArrayList<Int>()
        array.add(sharedPref.getInt("posDistance",-1))
        array.add(sharedPref.getInt("posTime",-1))
        return array
    }

    private fun setPersistedItem(position: Int){
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        val editor = sharedPref.edit()
        editor.putInt("posDistance",position).apply()
        editor.putString("radius",spinner.selectedItem.toString()).apply()
    }

    private fun setPersistedTimeItem(position: Int){
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        val editor = sharedPref.edit()
        editor.putInt("posTime",position).apply()
        editor.putString("time",timeSpinner.selectedItem.toString()).apply()
    }

}