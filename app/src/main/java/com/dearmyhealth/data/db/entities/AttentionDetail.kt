package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttentionDetail(
    @PrimaryKey(autoGenerate = true)
    val attid: Int=0,
    val itemseq: String="",
    val typeCode: String="",
    val prhbtContent: String=""
)
