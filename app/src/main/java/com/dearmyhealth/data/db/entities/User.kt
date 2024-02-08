package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val joinedTime: Long,
    val userid: String,
    val password: String,
    val name: String,
    val age: Int,
    val gender: Gender?
) {
    enum class Gender {
        MALE, FEMALE
    }
}