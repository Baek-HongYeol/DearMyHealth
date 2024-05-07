package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val code: String? = null,
    val prodName: String,
    val entpName: String,
    val dosage: Double,
    val units: UNITS,
    val description: String?,
    val warning: String?
) {
    enum class UNITS {
        ML,
        MG,
        PILL
    }
}