package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alarm(
    @PrimaryKey val alarmId: Int,
    val dosageId: Int,
    val requestCode: Int,
    var time: Long,
    val description: String,
    var isEnabled: Boolean
)
