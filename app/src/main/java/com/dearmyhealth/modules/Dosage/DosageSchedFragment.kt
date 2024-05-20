package com.dearmyhealth.modules.Dosage

import MedicationAdapter
import MedicationViewModel
import MedicationViewModelFactory
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding
import com.dearmyhealth.databinding.ViewDosageScheduleItemBinding
import com.dearmyhealth.modules.Alarm.AlarmRepository
import com.dearmyhealth.modules.Dosage.activity.DosageActivity
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory
import com.dearmyhealth.service.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<Dosage>()
    private var medicationList = mutableListOf<Medication>()
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var medicationViewModel: MedicationViewModel
    private lateinit var dosageViewModel: DosageViewModel
    private lateinit var alarmRepository: AlarmRepository

    private var selectedMedication: Medication? = null

    interface DosageOperateClickListener { // dosage ItemView에 넘겨줄 클릭 리스너 정의
        fun onAlarmSwitchListener(dosage:Dosage, isEnabled:Boolean)
        fun onEditClickListener(dosage: Dosage)
        fun onDeleteClickListener(dosage: Dosage)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)

        medicationViewModel = ViewModelProvider(this, MedicationViewModelFactory(RepositoryProvider.medicationRepository))[MedicationViewModel::class.java]
        dosageViewModel = ViewModelProvider(this, DosageViewModelFactory(RepositoryProvider.dosageRepository))[DosageViewModel::class.java]
        alarmRepository = AlarmRepository.getInstance(requireContext())

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
            it.dosageScheduleIsalarm.visibility = View.GONE

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
            override fun onAlarmSwitchListener(dosage: Dosage, isEnabled: Boolean) {
                if(isEnabled) setAlarm(dosage)
                else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val alarm = AlarmRepository.getInstance(requireContext()).findByDosageId(dosage.dosageId)
                        cancelAlarm(alarm.alarmId)
                    }
                }
            }
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

                CoroutineScope(Dispatchers.IO).launch {
                    val alarms = alarmRepository.findByDosageId(dosage.dosageId)
                    alarmRepository.deleteAlarms(alarms)
                }
            }
        }) { dosageId -> // onBindViewListener
            CoroutineScope(Dispatchers.IO).launch {
                val alarmRepository = AlarmRepository.getInstance(requireContext())
                val alarm = alarmRepository.getEnabledAlarms().filter { v -> v.dosageId == dosageId }
                Log.d("DosageSchedule", "alarm: $alarm")
                Log.d("DosageSchedule", "dosagelist:  ${binding.dosageScheduleListRV.children.toList().size}")
                for(el in binding.dosageScheduleListRV.children) {
                    val binding = ViewDosageScheduleItemBinding.bind(el)
                    withContext(Dispatchers.Main) {
                        binding.dosageScheduleIsalarm.isChecked = true
                    }
                }
            }
        }
        dosageViewModel.dosageList.observe(viewLifecycleOwner) { list ->
            scheduleList = list.toMutableList()
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).list = list
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).notifyDataSetChanged()
        }
    }

    fun setAlarm(dosage: Dosage) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val calendar: Calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val minutes = hour*60+minute.toLong()
        var nextAlarmMinutes = dosage.dosageTime.fold(1440L) { acc, v ->
            if (v in (minutes + 1) until acc)
                v
            else acc
        }
        if(nextAlarmMinutes.toInt() == 1440) {
            nextAlarmMinutes = dosage.dosageTime[0]
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, nextAlarmMinutes.toInt()/60)
        calendar.set(Calendar.HOUR_OF_DAY, nextAlarmMinutes.toInt()%60)
        val requestId = dosage.dosageId*1000+dosage.dosageTime[0].toInt()

        val alarmRepository = AlarmRepository.getInstance(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.findByRequestId(requestId)
            if(alarm != null) {
                alarm.isEnabled = true
                alarm.time = calendar.timeInMillis
                alarmRepository.updateAlarm(alarm)
            }
            else
                alarmRepository.insertAlarm(requestId, dosage.dosageId, calendar.timeInMillis, dosage.name)
        }

        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("requestId", requestId)
            PendingIntent.getBroadcast(context, requestId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager!!.setInexactRepeating(AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

    suspend fun cancelAlarm(alarmId: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val alarmRepository = AlarmRepository.getInstance(requireContext())
        val alarm = alarmRepository.findByRequestId(alarmId) ?: return
        alarm.isEnabled = false
        alarmRepository.updateAlarm(alarm)
        val pendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, alarm.requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager?.cancel(pendingIntent)
    }

}