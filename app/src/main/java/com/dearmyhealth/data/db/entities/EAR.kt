package com.dearmyhealth.data.db.entities

import androidx.room.Entity

/**
 * Estimated Average Requirement : 평균 필요량
 * 집단의 50%가 만족하는 필요량.
 * 단, 에너지(칼로리)의 경우 필요 추정량(Estimated Energy Requirement)을 의미한다.
 */
@Entity(primaryKeys = ["gender", "age"])
data class EAR(
    val gender: String,
    val age: Float,
    val calories: Int?,
    val carbohydrate: Int?,
    val protein: Int?,
    val fat: Int?,
)
