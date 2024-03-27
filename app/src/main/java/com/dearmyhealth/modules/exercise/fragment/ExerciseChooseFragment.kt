package com.dearmyhealth.modules.exercise.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.databinding.FragmentExerciseChooseBinding
import com.dearmyhealth.modules.exercise.ui.ExerciseTypeListAdapter
import com.dearmyhealth.modules.exercise.viewmodel.ExerciseChooseViewModel

class ExerciseChooseFragment: Fragment() {

    private lateinit var binding: FragmentExerciseChooseBinding
    private lateinit var viewModel: ExerciseChooseViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseChooseBinding.inflate(inflater)
        viewModel = ViewModelProvider(this, ExerciseChooseViewModel.Factory(
            requireActivity().application
        ))[ExerciseChooseViewModel::class.java]

        binding.exerciseListRV.adapter = ExerciseTypeListAdapter(listOf()) { _, pos ->
            viewModel.selectedPosition.value = pos
        }
        binding.exerciseListRV.layoutManager = LinearLayoutManager(context)

        observeViewModel()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.supportedExerciseType.observe(viewLifecycleOwner) { types ->
            (binding.exerciseListRV.adapter as ExerciseTypeListAdapter).setData(types.toList())
        }

        viewModel.selectedPosition.observe(viewLifecycleOwner) { position ->
            (binding.exerciseListRV.adapter as ExerciseTypeListAdapter).setSelectedItem(position)
        }
    }

}