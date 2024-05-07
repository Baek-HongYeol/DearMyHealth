package com.dearmyhealth.modules.Dosage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.modules.Dosage.repository.DosageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DosageViewModel(private val repository: DosageRepository) : ViewModel() {
    fun saveDosage(dosage: Dosage) {
        viewModelScope.launch {
            repository.insertDosage(dosage)
        }
    }
}
