package com.dearmyhealth.modules.exercise.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentExerciseDetailsBinding
import com.dearmyhealth.modules.exercise.model.ExerciseSession

class ExerciseDetailFragment: Fragment() {

    lateinit var binding: FragmentExerciseDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExerciseDetailsBinding.inflate(inflater)
        binding.isEmpty = true
        if(ExerciseSession.currentExerciseType != null) {
            binding.isEmpty = false
            binding.todayActStatusIV.setImageDrawable(
                ExerciseSession.getDrawableOf(requireContext(), ExerciseSession.currentExerciseType!!)
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