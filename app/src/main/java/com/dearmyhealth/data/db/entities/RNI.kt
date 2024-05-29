package com.dearmyhealth.data.db.entities

import androidx.room.Entity

/**
 * Recommended Nutrient Intake : 권장 섭취량
 * 집단의 97%가 필요량을 만족하는 섭취량을 의미한다.
 */
@Entity(primaryKeys = ["gender", "age"])
data class RNI(
    val gender: String,
    val age: Float,
    val calories: Int?,
    val carbohydrate: Int?,
    val protein: Int?,
    val fat: Int?,
    val water_liquid: Int?,
    val water_total: Int?
)
