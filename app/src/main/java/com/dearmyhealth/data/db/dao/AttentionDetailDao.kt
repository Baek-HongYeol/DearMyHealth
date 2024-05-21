package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication

@Dao
interface AttentionDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(attentionDetail: AttentionDetail)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<AttentionDetail>): List<Long>
    @Update
    fun update(attentionDetail: AttentionDetail)
    @Query("SELECT * FROM attentionDetail WHERE attid=:id")
    suspend fun find(id: Int) : AttentionDetail
    @Query("SELECT * FROM attentionDetail WHERE itemSeq = :itemSeq")
    suspend fun findByItemSeq(itemSeq: String): List<AttentionDetail>
    @Query("SELECT * FROM attentionDetail WHERE typeName = :typeName")
    suspend fun findByTypeName(typeName: String): List<AttentionDetail>
    @Delete
    fun delete(attentionDetail: AttentionDetail)
}