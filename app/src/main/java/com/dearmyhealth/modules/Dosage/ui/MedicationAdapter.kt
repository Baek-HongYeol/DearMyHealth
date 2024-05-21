import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Medication

class MedicationAdapter :
    ListAdapter<Medication, MedicationAdapter.MedicationViewHolder>(MedicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.view_medication_item, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.medication_name)
        private val detailsTextView: TextView = itemView.findViewById(R.id.medication_details)
        fun bind(medication: Medication) {
            nameTextView.text = medication.prodName
            detailsTextView.text = "Type: ${medication.typeName}\n" +
                    "Warning: ${medication.warning}"
        }
    }

    class MedicationDiffCallback : DiffUtil.ItemCallback<Medication>() {
        override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem.itemSeq == newItem.itemSeq
        }

        override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem == newItem
        }
    }
}
