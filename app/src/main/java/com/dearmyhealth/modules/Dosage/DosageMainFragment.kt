package com.dearmyhealth.modules.Dosage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.MyApplication
import com.dearmyhealth.R
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.views.DosageAlarm
import com.dearmyhealth.databinding.FragmentDosageMainBinding
import com.dearmyhealth.modules.Alarm.AlarmRepository
import com.dearmyhealth.modules.Dosage.ui.DosageAlarmListAdapter
import com.dearmyhealth.service.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DosageMainFragment: Fragment() {

    private lateinit var binding:FragmentDosageMainBinding

    private lateinit var alarmRepository: AlarmRepository

    private val alarms = AppDatabase.getDatabase(MyApplication.ApplicationContext()).dosageAlarmDao()
        .findIsEnabledLive(true)
    private var list: List<DosageAlarm> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDosageMainBinding.inflate(layoutInflater)
        binding.backSq2.setOnClickListener{
            findNavController().navigate(R.id.action_dosagescreen_to_dosagesched)
        }

        binding.dosageAlarmRV.layoutManager = LinearLayoutManager(context)
        binding.dosageAlarmRV.adapter = DosageAlarmListAdapter(list) { item, isEnabled ->
            if(isEnabled) setAlarm(item)
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = alarmRepository.findByRequestId(item.requestCode)
                    alarm?.let {
                        cancelAlarm(alarm.requestCode)
                    }
                }
            }
        }

        alarms.observe(viewLifecycleOwner) { values ->
            list = values
            (binding.dosageAlarmRV.adapter as DosageAlarmListAdapter).apply{
                list = values
                notifyDataSetChanged()
            }
        }

        return binding.root
    }

    fun setAlarm(dosage: DosageAlarm) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val calendar: Calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val minutes = hour*60+minute.toLong()
        var nextAlarmMinutes = dosage.dosageTime.fold(1440) { acc, v ->
            if (v in (minutes + 1) until acc)
                v
            else acc
        }
        if(nextAlarmMinutes == 1440) {
            nextAlarmMinutes = dosage.dosageTime[0]
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, nextAlarmMinutes.toInt()/60)
        calendar.set(Calendar.MINUTE, nextAlarmMinutes.toInt()%60)
        var requestId = dosage.dosageId*1000+dosage.dosageTime[0].toInt()

        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.findByRequestId(dosage.requestCode)
            if(alarm != null) {
                alarm.isEnabled = true
                alarm.time = calendar.timeInMillis
                alarmRepository.updateAlarm(alarm)
                requestId = alarm.requestCode
            }
            else
                alarmRepository.insertAlarm(requestId, dosage.dosageId, calendar.timeInMillis, dosage.name)

            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra("requestId", requestId)
                intent.putExtra("dosageName", dosage.name)
                PendingIntent.getBroadcast(context, requestId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager!!.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
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