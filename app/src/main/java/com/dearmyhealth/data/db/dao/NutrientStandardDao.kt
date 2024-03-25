package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.dearmyhealth.data.db.entities.EAR
import com.dearmyhealth.data.db.entities.RNI

@Dao
interface NutrientStandardDao {
    @Query("SELECT * From EAR WHERE gender LIKE :gender AND age>:age ORDER BY age ASC LIMIT 1")
    suspend fun loadEAR(gender: String, age: Float): EAR

    @Query("SELECT * FROM RNI WHERE gender LIKE :gender AND age>:age ORDER BY age ASC LIMIT 1")
    suspend fun loadRNI(gender: String, age: Float): RNI
}