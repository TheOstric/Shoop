package com.example.myapplication.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.DataEntity
import com.example.myapplication.database.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModel(application: Application) : AndroidViewModel(application)  {

    private var mRepository: DataRepository = DataRepository(application, viewModelScope)

    fun getAllData() : LiveData<List<DataEntity>>{
        return mRepository.getAllData()
    }

    fun insert(dataEntity: DataEntity){
        mRepository.insert(dataEntity)
    }

    suspend fun getMaxId() : Int{
        return mRepository.maxId()
    }

}