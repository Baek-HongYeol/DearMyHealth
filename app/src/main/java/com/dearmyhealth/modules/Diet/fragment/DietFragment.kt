package com.dearmyhealth.modules.Diet.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDietBinding
import com.dearmyhealth.modules.Diet.activity.DietCreateActivity
import com.dearmyhealth.modules.Diet.viewmodel.DietViewModel
import com.dearmyhealth.modules.Diet.model.Nutrients
import com.dearmyhealth.modules.Diet.ui.NutritionStandardReferenceView
import com.dearmyhealth.modules.Diet.ui.TodayNutritionItemView
import splitties.activities.start

class DietFragment : Fragment() {

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

        binding.todayDietHelpTV.setOnClickListener {
            val view = NutritionStandardReferenceView(requireContext())
            AlertDialog.Builder(requireContext())
                .setView(view)
                .show()
        }

        binding.createDietCL.setOnClickListener {
            context?.start<DietCreateActivity>()
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

    private fun getNutrientString(name: String) : String {
        val resId = Nutrients.resourceIds[name]

        return if (resId != null) getString(resId)
        else name
    }
}