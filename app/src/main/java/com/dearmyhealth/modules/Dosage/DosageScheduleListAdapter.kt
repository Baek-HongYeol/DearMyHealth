package com.dearmyhealth.modules.Dosage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.databinding.ViewDosageScheduleItemBinding

class DosageScheduleListAdapter(val list: List<Dosage>): RecyclerView.Adapter<DosageScheduleListAdapter.ViewHodler>() {
    class ViewHodler(val binding: ViewDosageScheduleItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodler {
        val binding = ViewDosageScheduleItemBinding.inflate(LayoutInflater.from(parent.context))

        return ViewHodler(binding)
    }

    override fun onBindViewHolder(holder: ViewHodler, position: Int) {
        holder.binding.dosageSchduleName.text = list[position].name
        holder.binding.dosageScheduleOptionIV.setOnClickListener {
            // TODO 수정/삭제 메뉴 보이기 with item_position
        }
    }

    override fun getItemCount(): Int = list.size

}