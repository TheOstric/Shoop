package com.example.myapplication.ui.home

interface ReorderAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position:Int)
}