package com.dearmyhealth.modules.Dosage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.modules.Dosage.repository.MedicationRepository
import kotlinx.coroutines.launch

//약물 검색 기능, LiveData 이용
class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {
    val medications = MutableLiveData<List<Medication>>()

    fun searchMedications(query: String) {
        viewModelScope.launch {
            val results = repository.searchMedications(query)
            medications.postValue(results)
        }
    }
}
