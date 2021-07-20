package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class MainListAdapter(context: Context?, textViewResourceId: Int, objects: List<String?>?) :
    ArrayAdapter<String?>(
        context!!, textViewResourceId, objects!!
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertView: View?
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = inflater.inflate(R.layout.list_item_main, null)
        val textView = convertView.findViewById<View>(R.id.text_view_item_main) as TextView
        val name = getItem(position)
        textView.text = name
        val imageView: ImageView = convertView.findViewById(R.id.image_view_item_main)
        when (name) {
            "Supermercato" -> {
                convertView.setBackgroundResource(R.drawable.radius)
                imageView.setImageResource(R.drawable.ic_market)
            }
            "Farmacia" -> {
                convertView.setBackgroundResource(R.drawable.radius_teal)
                imageView.setImageResource(R.drawable.ic_pharma)
            }

            "Benzinaio" -> {
                convertView.setBackgroundResource(R.drawable.radius_gas)
                imageView.setImageResource(R.drawable.ic_gpl)
            }
            "Ospedale" -> {
                convertView.setBackgroundResource(R.drawable.radius_hosp)
                imageView.setImageResource(R.drawable.ic_hospital)
            }
        }
        return convertView
    }
}