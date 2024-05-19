package com.dearmyhealth.modules.Dosage

import MedicationAdapter
import MedicationViewModel
import MedicationViewModelFactory
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory
import com.dearmyhealth.modules.Dosage.activity.DosageActivity

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<Dosage>()
    private var medicationList = mutableListOf<Medication>()
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var medicationViewModel: MedicationViewModel
    private lateinit var dosageViewModel: DosageViewModel

    private var selectedMedication: Medication? = null

    interface DosageOperateClickListener { // editOrDelete view에 넘겨줄 클릭 리스너 정의
        fun onEditClickListener(dosage: Dosage): Unit
        fun onDeleteClickListener(dosage: Dosage): Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)

        medicationViewModel = ViewModelProvider(this, MedicationViewModelFactory(RepositoryProvider.medicationRepository))[MedicationViewModel::class.java]
        dosageViewModel = ViewModelProvider(this, DosageViewModelFactory(RepositoryProvider.dosageRepository))[DosageViewModel::class.java]


        // 약물 목록 초기화
        setupSearchFunctionality()
        binding.dosageMedSearchResultRV.layoutManager = LinearLayoutManager(context)
        binding.dosageMedSearchResultRV.adapter = MedicationListAdapter(medicationList) { med ->
            selectedMedication = med
        }

        if (medicationList.isEmpty()) {
            binding.dosageMedSearchResultRV.visibility = View.GONE
            binding.medNoSearchResultTV.visibility = View.VISIBLE
        } else {
            binding.medNoSearchResultTV.visibility = View.GONE
            binding.dosageMedSearchResultRV.visibility = View.VISIBLE
        }


        // 복약 일정 목록 초기화
        setupScheduleFunctionality()
        binding.dosageScheduleListRV.layoutManager = LinearLayoutManager(context)

        binding.scheduleDefaultItem.let {
            it.dosageSchduleName.visibility = View.GONE
            it.dosageSchedulePeriod.visibility = View.GONE
            it.dosageScheduleDosage.visibility = View.GONE
            it.dosageScheduleDosageAmount.visibility = View.GONE

            it.dosageScheduleAddText.visibility = View.VISIBLE
            it.dosageSchedultAddIcon.visibility = View.VISIBLE
        }
        binding.scheduleDefaultItem.root.setOnClickListener {
            Intent(requireContext(), DosageActivity::class.java).apply {
                putExtra("operation","CREATE")
                putExtra("medName", selectedMedication?.prodName ?: "")
                putExtra("medItemSeq", selectedMedication?.itemSeq ?: "")
                startActivity(this)
            }
        }


        return binding.root
    }

//    private fun setupRecyclerView() {
//        medicationAdapter = MedicationAdapter()
//        binding.dosageMedSearchResultRV.apply {
//            layoutManager = LinearLayoutManager(context)
//            adapter = medicationAdapter
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupSearchFunctionality() {
        binding.search.setOnClickListener {
            val searchQuery = binding.medSearchEditText.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                medicationViewModel.searchMedications(searchQuery)
            }
        }

        medicationViewModel.medications.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    medicationList.clear()
                    medicationList.addAll(result.data)
                    (binding.dosageMedSearchResultRV.adapter
                            as MedicationListAdapter).notifyDataSetChanged()

                    binding.medNoSearchResultTV.visibility = View.GONE
                    binding.dosageMedSearchResultRV.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    Log.d("DosageSchedFragment", "search result not found")
                    binding.medNoSearchResultTV.visibility = View.VISIBLE
                    binding.dosageMedSearchResultRV.visibility = View.GONE
                }
                Result.Loading -> {
                }
                //else -> {}
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setupScheduleFunctionality() {
        binding.dosageScheduleListRV.adapter = DosageScheduleListAdapter(scheduleList, object: DosageOperateClickListener{
            override fun onEditClickListener(dosage: Dosage) {
                Intent(requireContext(), DosageActivity::class.java).apply {
                    putExtra("operation","EDIT")
                    putExtra("dosageId", dosage.dosageId)
                    startActivity(this)
                }
            }
            override fun onDeleteClickListener(dosage: Dosage) {
                dosageViewModel.deleteDosage(dosage)
                Toast.makeText(requireContext(), "삭제가 완료되었습니다.", Toast.LENGTH_LONG).show()
            }
        })
        dosageViewModel.dosageList.observe(viewLifecycleOwner) { list ->
            scheduleList = list.toMutableList()
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).list = list
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).notifyDataSetChanged()
        }
    }

}