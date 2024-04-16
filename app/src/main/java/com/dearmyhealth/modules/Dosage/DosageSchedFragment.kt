package com.dearmyhealth.modules.Dosage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.databinding.FragmentDosageScheduleBinding

class DosageSchedFragment: Fragment() {
    private lateinit var binding: FragmentDosageScheduleBinding
    private var scheduleList = mutableListOf<Dosage>()
    private var medicationList = listOf<Medication>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDosageScheduleBinding.inflate(layoutInflater)

        binding.dosageScheduleListRV.layoutManager = LinearLayoutManager(context)
        binding.dosageScheduleListRV.adapter = DosageScheduleListAdapter(scheduleList)

        binding.defaultItem.let {
            it.dosageSchduleName.visibility = View.GONE
            it.dosageSchedulePeriod.visibility = View.GONE
            it.dosageScheduleDosage.visibility = View.GONE

            it.dosageScheduleAddText.visibility=View.VISIBLE
            it.dosageSchedultAddIcon.visibility=View.VISIBLE
        }

        if(medicationList.isEmpty()) {
            binding.dosageMedSearchResultRV.visibility = View.GONE
            binding.medNoSearchResultTV.visibility = View.VISIBLE
        }
        else {
            binding.medNoSearchResultTV.visibility = View.GONE
            binding.dosageMedSearchResultRV.visibility = View.VISIBLE
        }


        return binding.root
    }
}