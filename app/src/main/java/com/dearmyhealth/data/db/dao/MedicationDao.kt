package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.Medication

@Dao
interface MedicationDao {
    @Insert
    fun insert(medication: Medication)

    @Update
    fun update(medication: Medication)

    @Query("SELECT * FROM medication WHERE medId=:id")
    suspend fun find(id: Int) : Medication

    @Query("SELECT * FROM medication WHERE prodName LIKE (:prodName)")
    suspend fun findByProdName(prodName:String) : List<Medication>

    @Query("SELECT * FROM medication WHERE entpName LIKE (:entpName)")
    suspend fun findByEntpName(entpName:String) : List<Medication>

    @Query("SELECT dosage FROM medication WHERE medId=:id")
    suspend fun dosage(id: Int) : Double

    @Query("SELECT units FROM medication WHERE medId=:id")
    suspend fun units(id: Int) : Medication.UNITS

    @Delete
    fun delete(medication: Medication)
}