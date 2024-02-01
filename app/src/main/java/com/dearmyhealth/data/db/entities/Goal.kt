package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["user"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"], childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Goal(
    @PrimaryKey val goalId: Int,
    val user: Int,
    val goalName: String,
    val cal: Int,
    val time: Int
)
