package com.dearmyhealth.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.databinding.FragmentHomeBinding
import com.dearmyhealth.modules.Diet.ui.TodayNutrientsView

class HomeFragment : Fragment() {
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HomeFragment Created!")

        viewModel = ViewModelProvider(
            this, HomeViewModel.Factory(this.requireContext(), this)
        )[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(inflater)
        binding.homeExerciseStatus.isEmpty = true
        binding.homeExerciseStatus.isHome = true

        viewModel.observeTodayDiet()
        viewModel.todayNutrients.observe(viewLifecycleOwner) { nutrients ->
            val todayNutrientsView = TodayNutrientsView(requireContext(), binding.todayNutrient.root)
            todayNutrientsView.setNutrients(nutrients)

        }

        return binding.root
    }

}