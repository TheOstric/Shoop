package com.example.myapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM dataentity WHERE data LIKE :date")
    fun findByDate(date: String) : DataEntity

    @Query("SELECT * FROM dataentity")
    fun getAllData() : LiveData<List<DataEntity>>

    @Query("SELECT MAX(id) FROM dataentity")
    suspend fun getMaxId() : Int

    @Delete
    fun delete(entity: DataEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DataEntity)

    @Query("DELETE FROM dataentity WHERE timestamp <= :time ")
    suspend fun deleteByDate(time: Double)

    @Query("DELETE FROM dataentity WHERE id LIKE :id")
    suspend fun deleteById(id: Int)
}