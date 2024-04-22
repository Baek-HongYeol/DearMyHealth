package com.dearmyhealth.modules.Dosage.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.databinding.ActivitySearchBinding
import com.dearmyhealth.modules.Dosage.ui.MedicationAdapter
import com.dearmyhealth.modules.Dosage.viewmodel.MedicationViewModel


//약물 검색 UI
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: MedicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = MedicationAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.searchButton.setOnClickListener {
            viewModel.searchMedications(binding.searchField.text.toString())
        }

        viewModel.medications.observe(this) { medications ->
            adapter.submitList(medications)
        }
    }
}