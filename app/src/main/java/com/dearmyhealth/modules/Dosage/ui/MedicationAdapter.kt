package com.dearmyhealth.modules.Dosage.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.health.platform.client.proto.Internal
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.ViewItemMedicationBinding

class MedicationAdapter : ListAdapter<Medication, MedicationAdapter.MedicationViewHolder>(DiffCallback()) {

    class MedicationViewHolder(private var binding: ViewItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.medName.text = medication.prodName

        // TO DO: 다른 뷰 바인딩 추가할거면
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewItemMedicationBinding.inflate(layoutInflater, parent, false)
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Medication>() {
        override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem == newItem
        }
    }
}
