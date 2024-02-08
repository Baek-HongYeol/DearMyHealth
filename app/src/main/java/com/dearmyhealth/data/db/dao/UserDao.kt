package com.dearmyhealth.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.User

@Dao
interface UserDao {
        @Query("SELECT * FROM user")
        suspend fun getAll(): List<User>

        @Query("SELECT * FROM user WHERE uid IN (:uids)")
        suspend fun loadAllByUids(uids: IntArray): List<User>

        @Query("SELECT * FROM user WHERE uid LIKE :uid LIMIT 1")
        fun find(uid: Int): LiveData<User?>

        @Query("SELECT * FROM user WHERE userid LIKE :id LIMIT 1")
        fun findByUserId(id: String): LiveData<User?>

        @Insert
        fun insertAll(vararg users: User)

        @Update
        fun updateUser(user: User)

        @Delete
        fun delete(user: User)

}