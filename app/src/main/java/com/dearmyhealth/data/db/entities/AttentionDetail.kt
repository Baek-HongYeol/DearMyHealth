package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttentionDetail(
    @PrimaryKey(autoGenerate = true)
    var attid: Int=0,
    val itemSeq: String="",
    val typeName: String="",
    val prhbtContent: String=""
    //val mainIngr: String=""
) {
    override fun equals(other: Any?): Boolean {
        if (other !is AttentionDetail)
            return super.equals(other)
        val target: AttentionDetail = other
        return this.itemSeq==target.itemSeq && this.typeName==target.typeName && this.prhbtContent==target.prhbtContent
    }
}

