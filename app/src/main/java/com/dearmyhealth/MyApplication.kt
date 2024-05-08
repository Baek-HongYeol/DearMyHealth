package com.dearmyhealth

import android.app.Application
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider

class MyApplication: Application() {

    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        RepositoryProvider.initialize(this)
    }

    val database by lazy {
        AppDatabase.getDatabase(this)
    }
    companion object {
        private lateinit var INSTANCE: MyApplication
        fun ApplicationContext() = INSTANCE.applicationContext
    }
}