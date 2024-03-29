package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = arrayOf(DataEntity::class), version = 5)
abstract class ShoopDatabase : RoomDatabase() {
    abstract fun shoppingDao() : ShoppingDao

    companion object{
        private var INSTANCE: ShoopDatabase? = null
        fun getInstance(context: Context,
        coroutineScope: CoroutineScope): ShoopDatabase{
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    ShoopDatabase::class.java,
                    "shoop")
                    .addCallback(RoomDatabaseCallback(coroutineScope, context))
                    .build()
            }
            return INSTANCE as ShoopDatabase
        }
    }

    private class RoomDatabaseCallback(private val scope: CoroutineScope, context: Context ): RoomDatabase.Callback() {

            val context = context
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        val shoppingDao = database.shoppingDao()
                        deleteData(shoppingDao,context)
                    }
                }
            }

            private suspend fun deleteData(shoppingDao: ShoppingDao, context: Context){
                val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                val time = sharedPref.getString("time","empty")
                var timestamp = 0.0


                if (time == "3gg")
                    timestamp = 259200.0
                if (time == "5gg")
                    timestamp = 432000.0
                if (time == "10gg")
                    timestamp = 864000.0
                if (time == "15gg")
                    timestamp = 1296000.0
                if (time == "empty")
                    timestamp = 259200.0

                val now = Date().time / 1000 - timestamp

                shoppingDao.deleteByDate(now)
            }
    }
}