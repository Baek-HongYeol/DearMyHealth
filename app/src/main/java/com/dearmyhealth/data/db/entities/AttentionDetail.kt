package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttentionDetail(
    @PrimaryKey(autoGenerate = true)
    val attid: Int=0,
    val itemSeq: String="",
    val typeName: String="",
    val prhbtContent: String=""
    //val mainIngr: String=""
)

