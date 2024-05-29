package com.dearmyhealth.modules.Dosage.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Dosage
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
    var operateClickListener: DosageSchedFragment.DosageOperateClickListener): RecyclerView.Adapter<DosageScheduleListAdapter.ViewHodler>() {
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
        Log.d("DosageScheListAdapter", "item name: ${list[position].name}")

        // 버튼 클릭 이벤트 처리하기
        holder.binding.dosageScheduleIsalarm.setOnCheckedChangeListener { _, isChecked ->
            operateClickListener.onAlarmSwitchListener(list[position], isChecked)
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