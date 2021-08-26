package com.example.myapplication.ui.home

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ListItemMainBinding
import com.example.myapplication.databinding.RecyclerviewItemBinding
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapter(private var entries: ArrayList<Service>,private var shoppingList: MutableList<String>, private var list: MutableList<String>, private var context: Context, private var layoutInflater: LayoutInflater) : RecyclerView.Adapter<RecyclerViewAdapter.MyHolder>(), ReorderAdapter {

    private var touchedTime = 0L
    private var touched: Boolean = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyHolder {
        val binding = ListItemMainBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val listItem = entries[position]

        holder.imageView.setImageResource(listItem.imageView)

        holder.textView.text = listItem.textView

        holder.itemView.setBackgroundResource(listItem.backgroundRes)


        holder.itemView.setOnClickListener {

            val touchedTimeNow = System.currentTimeMillis()

            if (touchedTimeNow - touchedTime < 300){
                touched = false
                touchedTime = touchedTimeNow
                val alertDialog = AlertDialog.Builder(context).create()
                val dialogView = layoutInflater.inflate(R.layout.visualize_service_dialog, null)
                alertDialog.setView(dialogView)
                val textView = dialogView.rootView.findViewById<TextView>(R.id.visualize_textview)
                alertDialog.setCancelable(true)
                when (listItem.textView) {
                    "Supermercato" -> {
                        alertDialog.setTitle("Lista supermercato")
                        alertDialog.setIcon(R.drawable.ic_market)
                        if (shoppingList[list.indexOf("supermarket")] != "") textView.text =
                            shoppingList[list.indexOf("supermarket")] else textView.setText(R.string.Vuoto)}

                    "Farmacia" -> {
                        alertDialog.setTitle("Lista farmacia")
                        alertDialog.setIcon(R.drawable.ic_pharma)
                        if (shoppingList[list.indexOf("pharmacy")] != "") textView.text =
                            shoppingList[list.indexOf("pharmacy")] else textView.setText(R.string.Vuoto) }
                    "Benzinaio" ->{
                        alertDialog.setTitle("Lista benzinaio")
                        alertDialog.setIcon(R.drawable.ic_gpl)
                        if (shoppingList[list.indexOf("gas_station")] != "") textView.text =
                            shoppingList[list.indexOf("gas_station")] else textView.setText(R.string.Vuoto) }
                    "Ospedale" -> {
                        alertDialog.setTitle("Lista ospedale")
                        alertDialog.setIcon(R.drawable.ic_hospital)
                        if (shoppingList[list.indexOf("hospital")] != "") textView.text =
                            shoppingList[list.indexOf("hospital")] else textView.setText(R.string.Vuoto) }
                }
                alertDialog.show()
            } else {
                touchedTime = touchedTimeNow

                holder.itemView.postDelayed(
                    {
                        if(touched){
                            val alertDialog = AlertDialog.Builder(context).create()
                            val dialogView = layoutInflater.inflate(R.layout.service_dialog, null)
                            alertDialog.setView(dialogView)
                            val editText = dialogView.rootView.findViewById<EditText>(R.id.edittext_dialog)
                            when (listItem.textView) {
                                "Supermercato" -> {
                                    alertDialog.setTitle("Lista supermercato")
                                    alertDialog.setIcon(R.drawable.ic_market)
                                    alertDialog.setCancelable(true)
                                    editText.hint = "Inserire la lista per il supermercato..."
                                    if (shoppingList[list.indexOf("supermarket")] != "") editText.setText(shoppingList[list.indexOf("supermarket")])
                                }
                                "Farmacia" -> {
                                    alertDialog.setTitle("Lista farmacia")
                                    alertDialog.setIcon(R.drawable.ic_pharma)
                                    alertDialog.setCancelable(true)
                                    editText.hint = "Inserire la lista per il farmacia..."
                                    if (shoppingList[list.indexOf("pharmacy")] != "") editText.setText(shoppingList[list.indexOf("pharmacy")])
                                }

                                "Benzinaio" -> {
                                    alertDialog.setTitle("Lista benzinaio")
                                    alertDialog.setIcon(R.drawable.ic_gpl)
                                    alertDialog.setCancelable(true)
                                    editText.hint = "Inserire la lista per il benzinaio..."
                                    if (shoppingList[list.indexOf("gas_station")] != "") editText.setText(shoppingList[list.indexOf("gas_station")])
                                }
                                "Ospedale" -> {
                                    alertDialog.setTitle("Lista ospedale")
                                    alertDialog.setIcon(R.drawable.ic_hospital)
                                    alertDialog.setCancelable(true)
                                    editText.hint = "Inserire la lista per l'ospedale..."
                                    if (shoppingList[list.indexOf("hospital")] != "") editText.setText(shoppingList[list.indexOf("hospital")])
                                }
                            }
                            //setto il pulsante positivo OK in modo che mi salvi le cose scritte nella lista all'interno
                            //dell'array shopping_list, in base a quale pulsante è stato cliccato avrà un indice diverso nell'array
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
                                val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                                    context.applicationContext
                                )
                                val editor = sharedPref.edit()
                                when (listItem.textView) {
                                    "Supermercato" ->{ shoppingList[list.indexOf("supermarket")] = editText.text.toString()
                                        editor.putString("market",editText.text.toString()).apply() }
                                    "Farmacia" -> { shoppingList[list.indexOf("pharmacy")] = editText.text.toString()
                                        editor.putString("pharmacy",editText.text.toString()).apply() }
                                    "Benzinaio" ->{ shoppingList[list.indexOf("gas_station")] = editText.text.toString()
                                        editor.putString("gastation",editText.text.toString()).apply() }
                                    "Ospedale" -> { shoppingList[list.indexOf("hospital")] = editText.text.toString()
                                        editor.putString("hospital",editText.text.toString()).apply() }
                                }
                            }
                            alertDialog.setButton(
                                AlertDialog.BUTTON_NEGATIVE,
                                "Cancel"
                            ) { dialog, which -> alertDialog.dismiss() }
                            alertDialog.show()
                            touched = false
                        }
                    }, 300L
                )
                touched = true
            }

        }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(entries,fromPosition,toPosition)
        Collections.swap(shoppingList,fromPosition,toPosition)
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition,toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        TODO("Not yet implemented")
    }

    class MyHolder(binding: ListItemMainBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageViewItemMain
        val textView: TextView = binding.textViewItemMain
    }
}