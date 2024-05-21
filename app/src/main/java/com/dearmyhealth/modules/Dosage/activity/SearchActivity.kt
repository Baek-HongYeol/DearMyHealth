package com.dearmyhealth.modules.Dosage.activity

import MedicationAdapter
import MedicationViewModel
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
        binding.dosageMedSearchResultRV.layoutManager = LinearLayoutManager(this)
            binding.dosageMedSearchResultRV.adapter = adapter
    }

    private fun setupSearch() {
        binding.search.setOnClickListener {
            val searchText = binding.medSearchEditText.text.toString()
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

                is Result.Loading -> {
                    // ProgresBar로 표시?
                    //binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}