import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {
    private val _medications = MutableLiveData<Result<List<Medication>>>()
    val medications: LiveData<Result<List<Medication>>> = _medications

    private val _selectedMedication = MutableLiveData<Medication>()
    val selectedMedication: LiveData<Medication> = _selectedMedication

    fun searchMedications(searchText: String) {
        _medications.value = Result.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = repository.searchMedications(searchText)
                if (results.isEmpty()) {
                    _medications.postValue(Result.Error(Exception("No results found")))
                } else {
                    _medications.postValue(Result.Success(results))
                }
            } catch (e: Exception) {
                _medications.postValue(Result.Error(e))
            }
        }
    }

    suspend fun getMedicationById(medId: Int): Medication? {
        return repository.getMedication(medId)
    }

    suspend fun getAttentionDetails(itemSeq: String): List<AttentionDetail> {
        return repository.getAttentionDetailsByItemSeq(itemSeq)
    }

    fun selectMedication(medication: Medication) {
        _selectedMedication.value = medication
    }
}
