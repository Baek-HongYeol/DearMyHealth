package com.dearmyhealth.modules.Diet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDietStatsBinding


class DietStatsFragment : Fragment() {

    private lateinit var binding : FragmentDietStatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDietStatsBinding.inflate(layoutInflater)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            DietStatsFragment()
    }
}