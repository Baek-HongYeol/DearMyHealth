package com.dearmyhealth.data.db.entities

import androidx.room.Entity

@Entity
data class EAR(
    val gender: User.Gender,
    val age: Float,
    val calories: Int?,
    val carbohydrate: Int?,
    val protein: Int?,
    val fat: Int?,
)
