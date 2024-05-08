package com.dearmyhealth.data.db.preload

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreloadTestUserCallback(private val context: Context): RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            prePopulateUsers(context)
        }
    }

    private suspend fun prePopulateUsers(context: Context) {
        val userDao = AppDatabase.getDatabase(context).userDao()
        userDao.insertAll(User(
            0, 0,
            "test", "test",
            "testName", 24, null)
        )

    }
}