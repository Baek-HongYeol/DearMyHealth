package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["medicationId", "user"])],
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["itemSeq"], childColumns = ["medicationId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"], childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Dosage(
    @PrimaryKey(autoGenerate = true) val dosageId: Int,
    val insertedTime: Long,
    val medicationId: String,
    val user: Int,
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val dosageTime: List<Int>,
    val dosage: Double?,
    val memo: String,
)
