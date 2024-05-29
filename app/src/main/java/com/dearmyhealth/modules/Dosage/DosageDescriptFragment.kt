package com.dearmyhealth.modules.Dosage

import MedicationViewModel
import MedicationViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageDescriptBinding
import com.dearmyhealth.modules.Dosage.activity.DosageActivity
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DosageDescriptFragment : Fragment() {
    private lateinit var binding: FragmentDosageDescriptBinding
    private lateinit var viewModel: MedicationViewModel
    private var selectedMedication: Medication? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageDescriptBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, MedicationViewModelFactory(RepositoryProvider.medicationRepository))[MedicationViewModel::class.java]

        val medId = arguments?.getInt("medId") ?: 0

        Log.d("DescriptionFragment", "medID: $medId")
        CoroutineScope(Dispatchers.IO).launch {
            val medication = viewModel.getMedicationById(medId)
            Log.d("DescriptionFragment", "Fetched Medication: $medication")
            withContext(Dispatchers.Main) {
                medication?.let { med ->
                    selectedMedication = med
                    displayMedicationDetails(med)
                    setAddButtonlistener()
                    med.itemSeq?.let { itemSeq ->
                        fetchAndDisplayAttentionDetails(itemSeq)
                    } ?: run {
                        Log.e("DosageDescriptFragment", "ItemSeq is null for medication: $med")
                    }
                } ?: run {
                    Log.e("DosageDescriptFragment", "Medication is null for medId: $medId")
                }
            }
        }

        return binding.root
    }

    private fun setAddButtonlistener() {
        binding.addMedicationScheduleButton.setOnClickListener {
            Intent(requireContext(), DosageActivity::class.java).apply {
                putExtra("operation","CREATE")
                putExtra("medName", selectedMedication?.prodName ?: "")
                putExtra("medItemSeq", selectedMedication?.itemSeq ?: "")
                startActivity(this)
            }
            findNavController().popBackStack()
        }
    }

    private fun fetchAndDisplayAttentionDetails(itemSeq: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val attentionDetails = viewModel.getAttentionDetails(itemSeq)
            Log.d("DescriptionFragment", "Fetched AttentionDetails: $attentionDetails")
            withContext(Dispatchers.Main) {
                displayAttentionDetails(attentionDetails)
            }
        }
    }

    private fun displayMedicationDetails(medication: Medication) {
        Log.d("DosageDescriptFragment", "Displaying medication details: $medication")
        binding.apply {
            medicationNameDetail.text = medication.prodName
            companyNameDetail.text = medication.entpName
            medicationChartDetail.text = medication.description
        }
    }

    private fun displayAttentionDetails(attentionDetails: List<AttentionDetail>) {
        Log.d("DosageDescriptFragment", "Displaying attention details: $attentionDetails")
        displayAttentionDetail(attentionDetails, "병용금기", binding.jointTaboo, binding.jointTabooDetail)
        displayAttentionDetail(attentionDetails, "특정연령대금기", binding.specificAgeTaboo, binding.specificAgeTabooDetail)
        displayAttentionDetail(attentionDetails, "임부금기", binding.pregnancyCaution, binding.pregnancyCautionDetail)
        displayAttentionDetail(attentionDetails, "용량주의", binding.capacityAttention, binding.capacityAttentionDetail)
        displayAttentionDetail(attentionDetails, "투여기간주의", binding.periodCaution, binding.periodCautionDetail)
        displayAttentionDetail(attentionDetails, "노인주의", binding.elderlyCaution, binding.elderlyCautionDetail)
        displayAttentionDetail(attentionDetails, "효능군중복", binding.effectDuplication, binding.effectDuplicationDetail)
        displayAttentionDetail(attentionDetails, "서방정분할주의", binding.releaseTabletSplitting, binding.releaseTabletSplittingDetail)
    }

    private fun displayAttentionDetail(
        attentionDetails: List<AttentionDetail>,
        typeName: String,
        titleView: TextView,
        detailView: TextView
    ) {
        val details = attentionDetails.filter { it.typeName == typeName }
        if (details.isNotEmpty()) {
            Log.d("DosageDescriptFragment", "Displaying $typeName details: $details")
            titleView.visibility = View.VISIBLE
            detailView.visibility = View.VISIBLE
            detailView.text = details.joinToString("\n") { it.prhbtContent }
        } else {
            Log.d("DosageDescriptFragment", "No $typeName details found.")
            titleView.visibility = View.GONE
            detailView.visibility = View.GONE
        }
    }
}
