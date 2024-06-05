package com.dearmyhealth.modules.Diet.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.databinding.FragmentDietBinding
import com.dearmyhealth.modules.Diet.activity.DietCreateActivity
import com.dearmyhealth.modules.Diet.ui.NutritionStandardReferenceView
import com.dearmyhealth.modules.Diet.ui.TodayNutrientsView
import com.dearmyhealth.modules.Diet.viewmodel.DietViewModel
import splitties.activities.start

class DietFragment : Fragment() {

    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: DietViewModel
    private lateinit var binding : FragmentDietBinding
    private val observer = Observer<List<Diet>> { value ->
        viewModel.calNuts(value)
    }

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

        binding.todayNutrient.todayCalories.textSize = 20f
        binding.todayNutrient.suggestedCalories.textSize = 20f

        observeViewModel()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.todayDiets.observe(viewLifecycleOwner, observer)
    }

    override fun onPause() {
        super.onPause()
        viewModel.todayDiets.removeObserver(observer)
    }

    fun observeViewModel() {
        viewModel.todayNutrients.observe(viewLifecycleOwner) { value ->
            val todayNutrientsView = TodayNutrientsView(requireContext(), binding.todayNutrient.root)
            todayNutrientsView.setNutrients(value)
        }
    }
}