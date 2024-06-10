package com.dearmyhealth.modules.Dosage.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.views.DosageAlarm
import com.dearmyhealth.databinding.ViewDosageScheduleItemBinding
import com.dearmyhealth.databinding.ViewEditOrDeleteBinding
import com.dearmyhealth.modules.Dosage.DosageSchedFragment
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DosageScheduleListAdapter(
    var list: List<Dosage>,
    var alarmList: List<DosageAlarm>?,
    var operateClickListener: DosageSchedFragment.DosageOperateClickListener
): RecyclerView.Adapter<DosageScheduleListAdapter.ViewHodler>() {
    class ViewHodler(val binding: ViewDosageScheduleItemBinding) : RecyclerView.ViewHolder(binding.root)

    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodler {
        val binding = ViewDosageScheduleItemBinding.inflate(LayoutInflater.from(parent.context))

        return ViewHodler(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHodler, position: Int) {
        // 레이아웃 속성 지정
        var layoutParams = holder.binding.root.layoutParams
        if (layoutParams == null)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        else
            layoutParams.apply {
                width = LayoutParams.MATCH_PARENT
                height = LayoutParams.WRAP_CONTENT
            }
        holder.binding.root.layoutParams = layoutParams

        // UI 데이터 넣어주기
        val item: Dosage = list[position]
        val startTime: String = dtf.format(
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(item.startTime), ZoneId.systemDefault())
        )
        val endTime: String = dtf.format(
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(item.endTime), ZoneId.systemDefault())
        )
        holder.binding.dosageSchduleName.text = item.name
        holder.binding.dosageSchedulePeriod.text = "$startTime ~ $endTime"
        holder.binding.dosageScheduleDosageAmount.text = "${(Math.round((item.dosage?:0.0)*100)/100)} 개"

        // 알람 스위치 이벤트 처리하기
        if (item.dosageTime.isEmpty())
            holder.binding.dosageScheduleIsalarm.isClickable = false
        else {
            holder.binding.dosageScheduleIsalarm.isClickable = true
            holder.binding.dosageScheduleIsalarm.setOnCheckedChangeListener { _, isChecked ->
                operateClickListener.onAlarmSwitchListener(list[position], isChecked)
            }
            alarmList?.find { alarm -> alarm.dosageId == item.dosageId }?.run {
                holder.binding.dosageScheduleIsalarm.isChecked = this.isEnabled
            }
        }

        holder.binding.dosageScheduleOptionIV.setOnClickListener {
            val editOrDeleteBinding = ViewEditOrDeleteBinding.inflate(LayoutInflater.from(holder.binding.root.context))
            val popup = PopupWindow(editOrDeleteBinding.root, 1,1, true)
            editOrDeleteBinding.apply{
                editView.setOnClickListener {
                    operateClickListener.onEditClickListener(list[position])
                    popup.dismiss()
                }
                delView.setOnClickListener {
                    operateClickListener.onDeleteClickListener(list[position])
                    popup.dismiss()
                }
            }
            popup.width = LayoutParams.WRAP_CONTENT
            popup.height = LayoutParams.WRAP_CONTENT
            popup.showAsDropDown(holder.binding.dosageScheduleOptionIV)
        }
    }

    override fun getItemCount(): Int = list.size

}