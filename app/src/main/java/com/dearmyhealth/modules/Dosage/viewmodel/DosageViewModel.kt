package com.dearmyhealth.modules.Dosage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.modules.Dosage.repository.DosageRepository
import com.dearmyhealth.modules.login.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DosageViewModel(private val repository: DosageRepository) : ViewModel() {

    private val _addResult: MutableLiveData<Result<Dosage>> = MutableLiveData()
    val addResult: LiveData<Result<Dosage>> = _addResult

    val dosageList: LiveData<List<Dosage>> = repository.listLiveDosage()

    val getResult: MutableLiveData<Dosage> = MutableLiveData()

    fun saveDosage(dosage: Dosage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDosage(dosage)
        }
    }

    suspend fun insertDosage(medicationCode: String?=null, name:String, startTime: Long, endTime: Long,
                     dosageTime: Long, dosage: Double?=1.0) {
        val result = repository.insert(medicationCode, name, startTime, endTime, dosageTime, dosage, Session.currentUser?.uid?:0)
        withContext(Dispatchers.Main) {
            _addResult.value = result
        }
    }

    fun updateDosage(dosage:Dosage) {
        repository.updateDosage(dosage)
    }

    fun deleteDosage(dosage: Dosage) {
        repository.deleteDosage(dosage)
    }

    suspend fun getDosage(dosageId: Int) {
        val result = repository.getDosage(dosageId)
        withContext(Dispatchers.Main) {
            getResult.value = result
        }
    }
}
