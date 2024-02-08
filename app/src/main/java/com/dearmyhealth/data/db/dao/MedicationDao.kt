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

    @Query("SELECT * FROM Medication WHERE medId=:id")
    suspend fun find(id: Int) : Medication

    @Query("SELECT * FROM Medication WHERE prodName LIKE (:prodName)")
    suspend fun findByprodName(prodName:String) : List<Medication>

    @Query("SELECT * FROM Medication WHERE entpName LIKE (:entpName)")
    suspend fun findByentpName(entpName:String) : List<Medication>

    @Query("SELECT dosage FROM Medication WHERE medId=:id")
    suspend fun dosage(id: Int) : Double

    @Query("SELECT units FROM Medication WHERE medId=:id")
    suspend fun unit(id: Int) : Medication.UNITS

    @Delete
    fun delete(medication: Medication)

}