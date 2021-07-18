package com.example.myapplication.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.DataEntity
import com.example.myapplication.databinding.RecyclerviewItemBinding
import com.example.myapplication.model.ViewModel

class DataEntityAdapter(private val context: Context?) : RecyclerView.Adapter<DataEntityAdapter.DataEntityHolder>() {
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mEntities: List<DataEntity>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataEntityHolder {
        val itemView: View = mInflater.inflate(R.layout.recyclerview_item,parent,false)
        return DataEntityHolder(itemView)
    }

    override fun onBindViewHolder(holder: DataEntityHolder, position: Int) {
        if (mEntities != null) {
            val current : DataEntity = mEntities!![position]
            holder.dateText.text = current.dateTime

            val market = current.sList
            val pharmacy = current.fList
            holder.visualize.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Lista della spesa")


                if (market == "" && pharmacy == "") {
                    builder.setMessage("La lista è vuota. Si prega di inserire elementi nella lista della spesa.")
                } else {

                    builder.setMessage(market + "\n" + pharmacy)
                }


                // Add OK and Cancel buttons
                builder.setPositiveButton("OK", null)

                builder.setNegativeButton("Cancel", null)

// Create and show the alert dialog
                val dialog = builder.create()
                dialog.show()
            }

            holder.copy.setOnClickListener {
                val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
                val editor = sharedPref.edit()
                editor.putString("market",market).apply()
                editor.putString("pharmacy",pharmacy).apply()
                Toast.makeText(context,"Copied.",Toast.LENGTH_SHORT).show()
            }

            holder.delete.setOnClickListener {

            }
        } else {
            holder.dateText.text = "No date"
        }
    }

    fun setEntities(entities: List<DataEntity>){
        mEntities = entities
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        if (mEntities != null)
            return mEntities!!.size
        else return 0
    }

    class DataEntityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dateText: TextView
        var delete: ImageButton
        var visualize: ImageButton
        var copy: ImageButton
        var binding: RecyclerviewItemBinding = RecyclerviewItemBinding.bind(itemView)

        init {
            dateText = binding.recyclerDate
            delete = binding.delete
            visualize = binding.visualize
            copy = binding.copy
        }
    }
}