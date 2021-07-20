package com.example.myapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.ArrayList

@Entity
data class DataEntity (
    @PrimaryKey var id: Int,
    @ColumnInfo (name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "data") var dateTime: String?,
    @ColumnInfo(name = "supermercato") var sList: String?,
    @ColumnInfo(name = "farmacia") var fList: String?,
    @ColumnInfo(name = "gas") var gList: String?,
    @ColumnInfo(name = "ospedale") var hList: String?
)