package com.example.myapplication.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MapsActivity
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
            val gas = current.gList
            val hosp = current.hList

            holder.visualize.setOnClickListener {
                val intent = Intent(context, DbEntryActivity::class.java)
                intent.putExtra("market",market)
                intent.putExtra("pharmacy",pharmacy)
                intent.putExtra("gas",gas)
                intent.putExtra("hospital",hosp)
                intent.putExtra("identifier",current.id)
                if (context != null) {
                    context.startActivity(intent)
                }
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
        return if (mEntities != null)
            mEntities!!.size
        else 0
    }

    class DataEntityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dateText: TextView
        var visualize: ImageButton
        var binding: RecyclerviewItemBinding = RecyclerviewItemBinding.bind(itemView)

        init {
            dateText = binding.recyclerDate
            visualize = binding.visualize
        }
    }
}