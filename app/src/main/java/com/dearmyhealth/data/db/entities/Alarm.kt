package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value=["dosageId"], unique = true)])
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Int,
    val dosageId: Int,
    val requestCode: Int,
    var time: Long,
    val description: String,
    var isEnabled: Boolean
)
