package com.example.myapplication.ui.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.example.myapplication.databinding.ActivityDbentryBinding
import com.example.myapplication.model.ViewModel
import kotlinx.coroutines.launch

class DbEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDbentryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDbentryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deleteButton = binding.delete
        val copyButton = binding.copy
        val textView = binding.listContent

        val mViewModel = ViewModel(application)
        val market = intent.extras?.getString("market")
        val pharmacy = intent.extras?.getString("pharmacy")
        val gas = intent.extras?.getString("gas")
        val hospital = intent.extras?.getString("hospital")
        val id = intent.extras?.getInt("identifier")

        if ( market != null && pharmacy != null){
            var message = ""
            message += "\nSupermercato\n"
            if ( market != "" )
                message += market
            message != "\n"

            message += "\nFarmacia\n"
            if ( pharmacy != "" )
                message += pharmacy
            message += "\n"

            message += "\nBenzinaio\n"
            if ( market != "" )
                message += market
            message != "\n"

            message += "\nOspedale\n"
            if ( pharmacy != "" )
                message += pharmacy
            message += "\n"

            textView.text = message
        } else {
            textView.text = "La lista è vuota."
        }

        copyButton.setOnClickListener {
            val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = sharedPref.edit()
            editor.putString("market",market).apply()
            editor.putString("pharmacy",pharmacy).apply()
            editor.putString("gastation",gas).apply()
            editor.putString("hospital",hospital).apply()
            Toast.makeText(this,"Copied.", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener {
            mViewModel.viewModelScope.launch {
                if (id != null) {
                    mViewModel.deleteById(id)
                    this@DbEntryActivity.finish()
                } else {
                    Toast.makeText(this@DbEntryActivity,"Errore nel corretto caricamento della pagina, tornare indietro e riprovare.",Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}