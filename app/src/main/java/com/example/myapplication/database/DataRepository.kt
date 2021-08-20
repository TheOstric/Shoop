package com.example.myapplication.database

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope

class DataRepository(application: Application, viewModelScope: CoroutineScope) {
    private var shoppingDao: ShoppingDao

    init {
        val db: ShoopDatabase = ShoopDatabase.getInstance(application,viewModelScope)
        shoppingDao = db.shoppingDao()
    }

    fun getAllData(): LiveData<List<DataEntity>>{
        return shoppingDao.getAllData()
    }

    suspend fun insert(dataEntity: DataEntity){
        shoppingDao.insert(dataEntity)
    }

    suspend fun maxId() : Int {
        return shoppingDao.getMaxId()
    }

    suspend fun deleteById(id: Int){
        shoppingDao.deleteById(id)
    }
}