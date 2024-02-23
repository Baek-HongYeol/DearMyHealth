package com.dearmyhealth.modules.Diet.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDietBinding
import com.dearmyhealth.modules.Diet.DietViewModel
import com.dearmyhealth.modules.Diet.Nutrients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietFragment : Fragment() {

    companion object {
        fun newInstance() = DietFragment()
    }
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: DietViewModel
    private lateinit var binding : FragmentDietBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "DietFragment Created!")
        viewModel = ViewModelProvider(this,
                DietViewModel.Factory(this.requireContext(), this)
            )[DietViewModel::class.java]
        binding = FragmentDietBinding.inflate(layoutInflater)

        binding.todayDietCL.setOnClickListener {
            findNavController().navigate(R.id.action_dietScreen_to_dietDetailScreen)
        }

        binding.foodSearchButton.setOnClickListener {
            val edtx = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle("음식 검색")
                .setView(edtx)
                .setNegativeButton("취소"){ _, _ ->

                }
                .setPositiveButton("검색") {_, _ ->
                    searchFood(edtx.text.toString())
                }.show()
        }

        observeViewModel()
        viewModel.observeTodayDiet()


        return binding.root
    }

    fun observeViewModel() {
        viewModel.todayNutritions.observe(viewLifecycleOwner) { value ->
            binding.nutritionsLL.removeAllViews()
            val names = value.getPresentNutrientsNames()
            while(binding.nutritionsLL.childCount < names.size) {
                binding.nutritionsLL.addView(TodayNutritionItemView(requireContext()))
            }
            binding.todayCalories.text = value.calories.toString()

            var i = 0
            for( v in binding.nutritionsLL.children) {
                (v as TodayNutritionItemView)
                    .setChart(
                        getNutrientString(names[i]),
                        value.getNutrientValueByName(names[i])?.toFloat() ?: 0f,
                        0
                    )

                i++
            }
        }
    }

    private fun searchFood(name: String, listener: OnClickListener? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            var result = ArrayList<DialogListItem>()
            result.addAll(viewModel.searchFood(name).map { v -> DialogListItem(v) })
            withContext(Dispatchers.Main) {
                val recyclerView = RecyclerView(requireContext())
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = DialogListAdapter(result, listener)
                var layoutParams = WindowManager.LayoutParams()
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                AlertDialog.Builder(requireContext())
                    .setTitle("검색 결과")
                    .setView(recyclerView)
                    .setPositiveButton("닫기") { _, _ -> }
                    .show()
            }
        }
    }

    private fun getNutrientString(name: String) : String {
        val resId = Nutrients.resourceIds[name]

        return if (resId != null) getString(resId)
        else name
    }
}