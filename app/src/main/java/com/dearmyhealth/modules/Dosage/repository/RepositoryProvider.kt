package com.dearmyhealth.modules.Dosage.repository

import MedicationRepository
import android.content.Context
import com.dearmyhealth.api.RetrofitObject
import com.dearmyhealth.data.db.AppDatabase


object RepositoryProvider {
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context
    }

    private val apiService by lazy {
        RetrofitObject.apiService
    }

    private val medicationDao by lazy {
        AppDatabase.getDatabase(applicationContext).medicationDao()
    }

    private val dosageDao by lazy {
        AppDatabase.getDatabase(applicationContext).dosageDao()
    }

    private val attentionDetailDao by lazy {
        AppDatabase.getDatabase(applicationContext).attentionDetailDao()
    }

    val medicationRepository: MedicationRepository by lazy {
        MedicationRepository(apiService, medicationDao, attentionDetailDao)
    }

    val dosageRepository: DosageRepository by lazy {
        DosageRepository(dosageDao, medicationRepository)
    }
}

