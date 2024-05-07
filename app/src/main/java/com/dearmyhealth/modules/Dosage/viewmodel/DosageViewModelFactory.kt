package com.dearmyhealth.modules.Dosage.viewmodel

import MedicationViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.modules.Dosage.repository.DosageRepository

class DosageViewModelFactory(private val context: Context, private val repository: DosageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DosageViewModel::class.java)) {
            return DosageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}