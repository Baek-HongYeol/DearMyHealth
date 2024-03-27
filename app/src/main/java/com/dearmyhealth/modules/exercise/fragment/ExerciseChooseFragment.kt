package com.dearmyhealth.modules.exercise.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentExerciseChooseBinding
import com.dearmyhealth.modules.exercise.model.Exercise
import com.dearmyhealth.modules.exercise.ui.ExerciseTypeListAdapter
import com.dearmyhealth.modules.exercise.viewmodel.ExerciseChooseViewModel

class ExerciseChooseFragment: Fragment() {
    private val TAG = javaClass.simpleName

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

        // 리사이클러뷰 초기화
        binding.exerciseListRV.adapter = ExerciseTypeListAdapter(listOf()) { _, pos ->
            viewModel.selectedPosition.value = pos  // viewModel에 선택한 아이템 정보 전달
        }
        binding.exerciseListRV.layoutManager = LinearLayoutManager(context)

        observeViewModel()

        // 선택한 운동 저장
        binding.exerciseChooseApply.setOnClickListener {
            // 지금 진행 중인 운동이라면 무시
            if(Exercise.currentExerciseType ==
                viewModel.supportedExerciseType.value?.get(viewModel.selectedPosition.value!!)) {
                return@setOnClickListener
            }
            viewModel.setExercise()
            findNavController().popBackStack()
        }
        binding.exerciseChooseCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // 진행중인 운동 종료
        binding.exerciseFinishTV.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("운동을 종료하시겠습니까?")
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.apply) { _, _ -> viewModel.clearExercise() }
                .show()
        }

        return binding.root
    }

    private fun observeViewModel() {

        // 지원하는 운동 종류 가져오기
        viewModel.supportedExerciseType.observe(viewLifecycleOwner) { types ->
            (binding.exerciseListRV.adapter as ExerciseTypeListAdapter).setData(types.toList())
        }

        // 현재 진행 중인 운동 정보 가져오기
        viewModel.selectedExercise.observe(viewLifecycleOwner) { type ->
            if (type == null) {
                binding.exerciseStatusIV.setImageDrawable(null)
                binding.exerciseNowIs.text = "현재 진행중인 운동이 없습니다."
            }
            else {
                binding.exerciseStatusIV.setImageDrawable(
                    Exercise.getDrawableOf(requireContext(), type)
                )
                binding.exerciseNowIs.text = type.displayName
            }
        }

        // 어댑터에 현재 선택된 아이템 정보 설정
        viewModel.selectedPosition.observe(viewLifecycleOwner) { position ->
            (binding.exerciseListRV.adapter as ExerciseTypeListAdapter).setSelectedItem(position)
        }
    }

}