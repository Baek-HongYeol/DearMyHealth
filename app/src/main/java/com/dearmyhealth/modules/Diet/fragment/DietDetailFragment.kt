package com.dearmyhealth.modules.Diet.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDietDetailBinding
import com.dearmyhealth.modules.Diet.viewmodel.DietDetailViewModel

import com.google.android.material.tabs.TabLayout


class DietDetailFragment : Fragment() {

    private val TAG = "DietTodayFragment"

    private lateinit var viewModel : DietDetailViewModel
    private lateinit var binding : FragmentDietDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDietDetailBinding.inflate(layoutInflater)


        val fragment1 = DietTodayFragment()
        val fragment2 = DietStatsFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.diet_detail_container, fragment1).commit()

        val tabs: TabLayout = binding.todayDietTabs
        tabs.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position

                var selected: Fragment? = null
                if (position==0) selected = fragment1
                else if (position==1) selected = fragment2

                if (selected != null) {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.diet_detail_container, selected).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

}