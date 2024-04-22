package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.Dosage

@Dao
interface DosageDao {
    @Insert
    fun insert(dosage: Dosage)

    @Update
    fun update(dosage: Dosage)

    @Query("SELECT * FROM dosage WHERE dosageId=:dosageId")
    suspend fun getDosage(dosageId:Int) : Dosage?

    @Query("SELECT * FROM dosage WHERE user=:uid")
    suspend fun findDosageFor(uid: Int) : List<Dosage>

    @Query("SELECT * FROM dosage WHERE user=:uid AND endTime>:time")
    suspend fun findDosageForAfter(uid: Int, time: Long) : List<Dosage>

    @Delete
    fun delete(dosage: Dosage)
}