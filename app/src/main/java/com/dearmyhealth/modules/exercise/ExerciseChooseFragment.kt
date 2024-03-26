package com.dearmyhealth.modules.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dearmyhealth.databinding.FragmentExerciseChooseBinding

class ExerciseChooseFragment: Fragment() {

    lateinit var binding: FragmentExerciseChooseBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseChooseBinding.inflate(inflater)



        return binding.root
    }

}