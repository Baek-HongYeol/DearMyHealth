package com.dearmyhealth.data.db.entities

import androidx.room.Entity

@Entity(primaryKeys = ["gender", "age"])
data class EAR(
    val gender: String,
    val age: Float,
    val calories: Int?,
    val carbohydrate: Int?,
    val protein: Int?,
    val fat: Int?,
)
