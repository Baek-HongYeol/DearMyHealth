package com.dearmyhealth.modules.Dosage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding
import com.dearmyhealth.databinding.ViewDosageAddscheduleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<Dosage>()
    private var medicationList = listOf<Medication>()

    private var selectedMedication: Medication? = null

    private lateinit var dosageRepo: DosageRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)

        dosageRepo = DosageRepository(
            AppDatabase.getDatabase(requireActivity().applicationContext).dosageDao(),
            AppDatabase.getDatabase(requireActivity().applicationContext).medicationDao()
        )

        binding.dosageScheduleListRV.layoutManager = LinearLayoutManager(context)
        binding.dosageScheduleListRV.adapter = DosageScheduleListAdapter(scheduleList)
        binding.dosageMedSearchResultRV.adapter = MedicationListAdapter(medicationList) { med ->
            selectedMedication = med
        }

        binding.defaultItem.let {
            it.dosageSchduleName.visibility = View.GONE
            it.dosageSchedulePeriod.visibility = View.GONE
            it.dosageScheduleDosage.visibility = View.GONE

            it.dosageScheduleAddText.visibility=View.VISIBLE
            it.dosageSchedultAddIcon.visibility=View.VISIBLE
        }

        binding.defaultItem.root.setOnClickListener {
            val dialogBinding = ViewDosageAddscheduleBinding.inflate(layoutInflater)
            createSchedDialog(dialogBinding)
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .create().show()
        }

        if(medicationList.isEmpty()) {
            binding.dosageMedSearchResultRV.visibility = View.GONE
            binding.medNoSearchResultTV.visibility = View.VISIBLE
        }
        else {
            binding.medNoSearchResultTV.visibility = View.GONE
            binding.dosageMedSearchResultRV.visibility = View.VISIBLE
        }


        return binding.root
    }

    fun createSchedDialog(binding: ViewDosageAddscheduleBinding) {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-DD", Locale.getDefault())
        binding.name.text = selectedMedication?.prodName ?: ""
        binding.amount.text = "1"

        var startdate = OffsetDateTime.now()
        var endDate = OffsetDateTime.now().plusHours(24)
        binding.box3.text = dtf.format(startdate)
        binding.box4.text = dtf.format(endDate)

        binding.dosageAddStartDatepicker.setOnDateChangedListener { _, year, month, day  ->
            startdate = OffsetDateTime.now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
            binding.box3.text = dtf.format(startdate)
        }
        binding.dosageAddEndDatepicker.setOnDateChangedListener { _, year, month, day  ->
            endDate = OffsetDateTime.now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
            binding.box4.text = dtf.format(endDate)
        }

        val dosageTime = 0L
        binding.dosageAddButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                insertDosage(
                    selectedMedication?.code, binding.name.text.toString(),
                    startdate.toInstant().toEpochMilli(), endDate.toInstant().toEpochMilli(),
                    dosageTime, binding.amount.text.toString().toDouble()
                )
            }
        }
    }

    suspend fun insertDosage(medicationCode: String?=null, name:String, startTime: Long, endTime: Long,
                             dosageTime: Long, dosage: Double?=1.0) {

        val result =  dosageRepo.insert(medicationCode, name, startTime, endTime, dosageTime, dosage)
        if (result is Result.Success)
            Log.e("DosageSchedFragment", "dosage insertion failed: ${(result as Result.Error).exception}")
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "식단 데이터를 입력했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}