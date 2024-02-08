package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Symptom(
    @PrimaryKey val symptomId: Int,
    val name: String,
    val description: String,
    val category: String
)
