package com.dearmyhealth.modules.Dosage.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.views.DosageAlarm
import com.dearmyhealth.databinding.ViewDosageScheduleItemBinding
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DosageAlarmListAdapter(
    var list: List<DosageAlarm>,
    val alarmCheckListener: (item: DosageAlarm, isChecked: Boolean)-> Unit
) : RecyclerView.Adapter<DosageAlarmListAdapter.ViewHolder>(){
    class ViewHolder(val binding: ViewDosageScheduleItemBinding) : RecyclerView.ViewHolder(binding.root)
    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewDosageScheduleItemBinding.inflate(LayoutInflater.from(parent.context))

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 레이아웃 속성 지정
        var layoutParams = holder.binding.root.layoutParams
        if (layoutParams == null)
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        else
            layoutParams.apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
                height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
        holder.binding.root.layoutParams = layoutParams

        // UI 데이터 넣어주기
        val item: DosageAlarm = list[position]
        val startTime: String = dtf.format(
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(item.startTime), ZoneId.systemDefault())
        )
        val endTime: String = dtf.format(
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(item.endTime), ZoneId.systemDefault())
        )
        holder.binding.dosageSchduleName.text = item.name
        holder.binding.dosageSchedulePeriod.text = "$startTime ~ $endTime"
        holder.binding.dosageScheduleDosageAmount.text = "${(Math.round((item.dosage?:0.0)*100)/100)} 개"
        Log.d("DosageScheListAdapter", "item name: ${list[position].name}")

        holder.binding.dosageScheduleIsalarm.isChecked = item.isEnabled
        // 버튼 클릭 이벤트 처리하기
        holder.binding.dosageScheduleIsalarm.setOnCheckedChangeListener { _, isChecked ->
            alarmCheckListener(item, isChecked)
        }
        holder.binding.dosageScheduleOptionIV.visibility = View.GONE
    }

    override fun getItemCount(): Int = list.size
}