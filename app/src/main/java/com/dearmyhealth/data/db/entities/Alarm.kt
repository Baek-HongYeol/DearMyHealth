package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alarm(
    @PrimaryKey val alarmId: Int,
    val requestCode: String,
    val time: Long,
    val description: String
)
