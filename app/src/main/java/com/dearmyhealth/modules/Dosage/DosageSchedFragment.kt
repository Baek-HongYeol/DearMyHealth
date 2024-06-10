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
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.R
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.data.db.views.DosageAlarm
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding
import com.dearmyhealth.modules.Alarm.AlarmRepository
import com.dearmyhealth.modules.Dosage.activity.DosageActivity
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.ui.DosageScheduleListAdapter
import com.dearmyhealth.modules.Dosage.ui.MedicationListAdapter
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory
import com.dearmyhealth.service.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<DosageAlarm>()
    private var medicationList = mutableListOf<Medication>()
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var medicationViewModel: MedicationViewModel
    private lateinit var dosageViewModel: DosageViewModel
    private lateinit var alarmRepository: AlarmRepository

    private var selectedMedication: Medication? = null

    interface DosageOperateClickListener { // dosage ItemView에 넘겨줄 클릭 리스너 정의
        fun onAlarmSwitchListener(dosage:DosageAlarm, isEnabled:Boolean)
        fun onEditClickListener(dosage: DosageAlarm)
        fun onDeleteClickListener(dosage: DosageAlarm)
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
            //특정 약물을 클릭하면 DosageDescriptFragment로 이동
            medicationViewModel.selectMedication(med)
            val bundle = bundleOf("medId" to med.medId)
            findNavController().navigate(R.id.action_dosagesched_to_dosagedescript, bundle)
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

        binding.medSearchEditText.setOnEditorActionListener { textView, i, _ ->
            when(i) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    if(textView.text.trim() == "")
                        return@setOnEditorActionListener true
                    val searchQuery = textView.text.toString().trim()
                    if (searchQuery.isNotEmpty()) {
                        medicationViewModel.searchMedications(searchQuery)
                    }
                }
            }
            return@setOnEditorActionListener true
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
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setupScheduleFunctionality() {
        binding.dosageScheduleListRV.adapter = DosageScheduleListAdapter(scheduleList, object: DosageOperateClickListener{
            override fun onAlarmSwitchListener(dosage: DosageAlarm, isEnabled: Boolean) {
                if(isEnabled) setAlarm(dosage)
                else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val alarm = alarmRepository.findByDosageId(dosage.dosageId)
                        alarm?.let {
                            cancelAlarm(alarm.requestCode)
                        }
                    }
                }
            }
            override fun onEditClickListener(dosage: DosageAlarm) {
                Intent(requireContext(), DosageActivity::class.java).apply {
                    putExtra("operation","EDIT")
                    putExtra("dosageId", dosage.dosageId)
                    startActivity(this)
                }
            }
            override fun onDeleteClickListener(dosage: DosageAlarm) {
                dosageViewModel.deleteDosage(dosage)
                Toast.makeText(requireContext(), "삭제가 완료되었습니다.", Toast.LENGTH_LONG).show()

                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = alarmRepository.findByDosageId(dosage.dosageId)
                    alarm?.let {
                        cancelAlarm(alarm.requestCode)
                        alarmRepository.deleteAlarms(alarm)
                    }
                }
            }
        })

        dosageViewModel.dosageAlarmList.observe(viewLifecycleOwner) { list ->
            scheduleList = list.toMutableList()
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).list = list
            (binding.dosageScheduleListRV.adapter as DosageScheduleListAdapter).notifyDataSetChanged()
        }
    }

    fun setAlarm(dosage: DosageAlarm) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        var datetime = OffsetDateTime.now()
        val minutes = TimeUnit.HOURS.toMinutes(datetime.hour.toLong()).toInt() + datetime.minute

        var nextAlarmMinutes = minutes

        for (time in dosage.dosageTime) {
            if (time > minutes) {
                nextAlarmMinutes = time
                break
            }
        }
        if (nextAlarmMinutes == minutes) {
            nextAlarmMinutes = dosage.dosageTime[0]
            datetime = datetime.plusDays(1)
        }
        val nextDatetime = datetime
            .withHour(nextAlarmMinutes/60)
            .withMinute(nextAlarmMinutes%60)
            .toInstant().toEpochMilli()

        var requestId = dosage.dosageId*1000+dosage.dosageTime[0]

        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.findByDosageId(dosage.dosageId)
            if(alarm != null) {
                alarm.isEnabled = true
                alarm.time = nextDatetime
                alarmRepository.updateAlarm(alarm)
                requestId = alarm.requestCode
            }
            else
                alarmRepository.insertAlarm(requestId, dosage.dosageId, nextDatetime, dosage.name)

            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra("requestId", requestId)
                intent.putExtra("dosageName", dosage.name)
                PendingIntent.getBroadcast(context, requestId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager!!.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                nextDatetime,
                alarmIntent
            )
        }
    }

    suspend fun cancelAlarm(requestId: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarm = alarmRepository.findByRequestId(requestId) ?: return

        alarm.isEnabled = false
        alarmRepository.updateAlarm(alarm)
        val pendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, alarm.requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager?.cancel(pendingIntent)
    }

}