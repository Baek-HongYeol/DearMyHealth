package com.dearmyhealth.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Query("SELECT * FROM medication")
    fun getAllMedications(): LiveData<List<Medication>>

    @Query("SELECT * FROM medication WHERE medId = :id")
    suspend fun find(id: Int): Medication?

    @Query("SELECT * FROM Medication WHERE id = :itemSeq")
    suspend fun findByItemSeq(itemSeq: String): List<Medication>

    @Query("SELECT * FROM medication WHERE prodName LIKE (:prodName)")
    suspend fun findByProdName(prodName: String): List<Medication>

    @Query("SELECT * FROM medication WHERE prodName LIKE :nameOr OR id = :seq")
    suspend fun findByNameOrSeq(nameOr: String, seq: String): List<Medication>

    @Query("SELECT * FROM Medication WHERE typeCode = :typeCode")
    suspend fun findByTypeCode(typeCode: String): List<Medication>

    @Query("SELECT * FROM Medication WHERE typeName = :typeName")
    suspend fun findByTypeName(typeName: String): List<Medication>

    @Query("SELECT dosage FROM medication WHERE medId = :id")
    suspend fun dosage(id: Int): Double

    @Query("SELECT units FROM Medication WHERE medId = :id")
    suspend fun findUnitsById(id: Int): String?

    @Delete
    suspend fun delete(medication: Medication): Int
}