import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.load.engine.Resource
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.launch

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {
    private val _medications = MutableLiveData<Result<List<Medication>>>()
    val medications: LiveData<Result<List<Medication>>> = _medications

    fun searchMedications(searchText: String) {
        _medications.value = Result.Loading
        viewModelScope.launch {
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
}
