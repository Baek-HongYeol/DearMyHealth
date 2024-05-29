package com.dearmyhealth.modules.Dosage.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

    override fun onBindViewHolder(holder: MedViewHolder, position: Int) {
        holder.binding.root.text = list[position].prodName
        holder.binding.root.setOnClickListener {
            listener(list[position])
        }
    }

    override fun getItemCount() = list.size
}