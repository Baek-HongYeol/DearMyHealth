package com.dearmyhealth.modules.Diet.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dearmyhealth.databinding.FragmentDietTodayBinding
import com.dearmyhealth.modules.Diet.viewmodel.DietDetailViewModel


class DietTodayFragment : Fragment() {

    private lateinit var binding: FragmentDietTodayBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDietTodayBinding.inflate(layoutInflater)


        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DietTodayFragment()
    }
}