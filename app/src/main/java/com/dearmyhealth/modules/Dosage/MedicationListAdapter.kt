package com.dearmyhealth.modules.Dosage


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.ViewSingleTextBinding

class MedicationListAdapter(val list: List<Medication>, val listener: ((Medication) -> Unit)): RecyclerView.Adapter<MedicationListAdapter.MedViewHolder>()  {
    class MedViewHolder(val binding: ViewSingleTextBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedViewHolder {
        val binding = ViewSingleTextBinding.inflate(LayoutInflater.from(parent.context))

        return MedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationListAdapter.MedViewHolder, position: Int) {
        holder.binding.root.text = list[position].prodName
        holder.binding.root.setOnClickListener {
            listener(list[position])
            holder.binding.root.setBackgroundColor(ContextCompat.getColor(holder.binding.root.context, R.color.second_panel))
        }
    }

    override fun getItemCount() = list.size
}