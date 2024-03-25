package com.dearmyhealth.modules.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentExerciseBinding

class ExerciseFragment: Fragment() {

    lateinit var binding: FragmentExerciseBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExerciseBinding.inflate(inflater)

        ExerciseButton.setupLayoutClickListener(
            requireContext(),
            Pair(R.id.constraintLayout7, R.id.exercise_chosen),
            Pair(R.id.constraintLayout8, R.id.second_FeatureTitle),
        )

        return binding.root
    }

}