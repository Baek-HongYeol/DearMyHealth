package com.dearmyhealth.data.db.views

import androidx.room.DatabaseView

@DatabaseView("SELECT dosage.dosageId, dosage.user, dosage.medicationId, dosage.name," +
        "dosage.dosage, dosage.dosageTime, dosage.startTime, dosage.endTime," +
        "alarm.requestCode, alarm.time, alarm.isEnabled FROM dosage " +
        "INNER JOIN alarm ON dosage.dosageId = alarm.dosageId")
data class DosageAlarm(
    val dosageId: Int,
    val user: Int,
    val medicationId: String,
    val name: String,
    val dosage: Double?,
    val dosageTime: List<Int>,
    val startTime: Long,
    val endTime: Long,
    val requestCode: Int,
    var time: Long,
    var isEnabled: Boolean
)