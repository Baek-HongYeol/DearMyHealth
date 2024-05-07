import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.Result
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: MedicationViewModel
    private lateinit var adapter: MedicationAdapter
    private lateinit var binding: FragmentDosageScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MedicationViewModel::class.java)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter()
        binding.dosageMedSearchResultRV.adapter = adapter
    }

    private fun setupSearch() {
        binding.search.setOnClickListener {
            val searchText = binding.editTextText.text.toString()
            if (searchText.isNotEmpty()) {
                viewModel.searchMedications(searchText)
            }
        }

        viewModel.medications.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    adapter.submitList(result.data)
                    binding.medNoSearchResultTV.visibility = View.GONE
                }
                is Result.Error -> {
                    binding.medNoSearchResultTV.apply {
                        text = result.exception.message ?: "검색 결과가 없습니다."
                        visibility = View.VISIBLE
                    }
                }
                Result.Loading -> {
                    // ProgresBar로 표시?
                    //binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}
