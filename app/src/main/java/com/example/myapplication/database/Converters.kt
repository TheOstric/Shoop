package com.example.myapplication.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object Converters {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        var listType = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        var gson = Gson()
        return gson.toJson(list)
    }
}