package com.dearmyhealth

import android.app.Application

class MyApplication: Application() {

    init {
        INSTANCE = this
    }

    companion object {
        private lateinit var INSTANCE: MyApplication
        fun ApplicationContext() = INSTANCE.applicationContext
    }
}