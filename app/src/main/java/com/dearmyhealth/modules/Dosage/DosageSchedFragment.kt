package com.dearmyhealth.modules.Dosage

import MedicationAdapter
import MedicationViewModel
import MedicationViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.MyApplication
import com.dearmyhealth.R
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding
import com.dearmyhealth.modules.Dosage.repository.DosageRepository
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<Dosage>()
    private var medicationList = listOf<Medication>()
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var medicationViewModel: MedicationViewModel
    private lateinit var dosageViewModel: DosageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)

        binding.dosageScheduleListRV.layoutManager = LinearLayoutManager(context)
        binding.dosageScheduleListRV.adapter = DosageScheduleListAdapter(scheduleList)

        binding.defaultItem.let {
            it.dosageSchduleName.visibility = View.GONE
            it.dosageSchedulePeriod.visibility = View.GONE
            it.dosageScheduleDosage.visibility = View.GONE

            it.dosageScheduleAddText.visibility = View.VISIBLE
            it.dosageSchedultAddIcon.visibility = View.VISIBLE
        }

        if (medicationList.isEmpty()) {
            binding.dosageMedSearchResultRV.visibility = View.GONE
            binding.medNoSearchResultTV.visibility = View.VISIBLE
        } else {
            binding.medNoSearchResultTV.visibility = View.GONE
            binding.dosageMedSearchResultRV.visibility = View.VISIBLE
        }


        medicationViewModel = ViewModelProvider(this, MedicationViewModelFactory(RepositoryProvider.medicationRepository)).get(MedicationViewModel::class.java)
        //dosageViewModel = ViewModelProvider(this, DosageViewModelFactory()).get(DosageViewModel::class.java)
        setupRecyclerView()
        setupSearchFunctionality()
        //setupSaveButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationAdapter()
        binding.dosageMedSearchResultRV.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = medicationAdapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.search.setOnClickListener {
            val searchQuery = binding.editTextText.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                medicationViewModel.searchMedications(searchQuery)
            }
        }

        medicationViewModel.medications.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    medicationAdapter.submitList(result.data)
                    binding.medNoSearchResultTV.visibility = View.GONE
                    binding.dosageMedSearchResultRV.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    binding.medNoSearchResultTV.text = getString(R.string.no_result)
                    binding.medNoSearchResultTV.visibility = View.VISIBLE
                    binding.dosageMedSearchResultRV.visibility = View.GONE
                }
                Result.Loading -> {
                }
                //else -> {}
            }
        }
    }
    /*private fun setupSaveButton() {
        binding.search.setOnClickListener {
            val dosage = Dosage()
                dosageViewModel.saveDosage(dosage)
        }
    }*/
}