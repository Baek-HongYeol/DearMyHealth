package com.dearmyhealth.modules.exercise.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentExerciseBinding
import com.dearmyhealth.modules.exercise.model.Exercise

class ExerciseFragment: Fragment() {

    lateinit var binding: FragmentExerciseBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExerciseBinding.inflate(inflater)
        binding.isEmpty = true
        if(Exercise.currentExerciseType != null) {
            binding.isEmpty = false
            binding.todayActStatusIV.setImageDrawable(
                Exercise.getDrawableOf(requireContext(), Exercise.currentExerciseType!!)
            )
        }

        binding.todayActStatusIV.setOnClickListener {
            findNavController().navigate(R.id.exerciseSettingScreen)
        }

        binding.exerciseNothingIV.setOnClickListener {
            findNavController().navigate(R.id.exerciseSettingScreen)
        }

        return binding.root
    }

}