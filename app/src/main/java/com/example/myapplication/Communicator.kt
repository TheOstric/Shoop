package com.example.myapplication

import com.example.myapplication.database.DataEntity

interface Communicator {
    fun passDataCom(list: DataEntity)
}