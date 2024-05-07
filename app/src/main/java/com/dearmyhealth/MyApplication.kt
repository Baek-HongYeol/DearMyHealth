package com.dearmyhealth

import android.app.Application
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider

class MyApplication: Application() {

    init {
        INSTANCE = this
    }

    companion object {
        private lateinit var INSTANCE: MyApplication
        fun ApplicationContext() = INSTANCE.applicationContext
    }
    override fun onCreate() {
        super.onCreate()
        RepositoryProvider.initialize(this)
    }
}