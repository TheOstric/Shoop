package com.example.myapplication.ui.home

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R


class ReorderHelper(val adapter : ReorderAdapter, private var shoppingList: MutableList<String>, private var list: MutableList<String>, private var context: Context, private var layoutInflater: LayoutInflater) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN

        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
       /* val alertDialog = AlertDialog.Builder(this.context).create()
        val dialogView = layoutInflater.inflate(R.layout.visualize_service_dialog, null)
        alertDialog.setView(dialogView)
        val textView = dialogView.rootView.findViewById<TextView>(R.id.visualize_textview)
        alertDialog.setCancelable(true)
        when (list[viewHolder.adapterPosition]) {
            "Supermercato" -> {
                alertDialog.setTitle("Lista supermercato")
                alertDialog.setIcon(R.drawable.ic_market)
                if (shoppingList[0] != "") textView.text =
                    shoppingList[0] else textView.setText(R.string.Vuoto)}

            "Farmacia" -> {
                alertDialog.setTitle("Lista farmacia")
                alertDialog.setIcon(R.drawable.ic_pharma)
                if (shoppingList[1] != "") textView.text =
                    shoppingList[1] else textView.setText(R.string.Vuoto) }
            "Benzinaio" ->{
                alertDialog.setTitle("Lista benzinaio")
                alertDialog.setIcon(R.drawable.ic_gpl)
                if (shoppingList[2] != "") textView.text =
                    shoppingList[2] else textView.setText(R.string.Vuoto) }
            "Ospedale" -> {
                alertDialog.setTitle("Lista ospedale")
                alertDialog.setIcon(R.drawable.ic_hospital)
                if (shoppingList[3] != "") textView.text =
                    shoppingList[3] else textView.setText(R.string.Vuoto) }
        }
        alertDialog.show()*/
    }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.5f
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
    }
}