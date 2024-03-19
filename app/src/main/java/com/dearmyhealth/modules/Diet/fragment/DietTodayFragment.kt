package com.dearmyhealth.modules.Diet.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.databinding.FragmentDietTodayBinding
import com.dearmyhealth.modules.Diet.model.Nutrients
import com.dearmyhealth.modules.Diet.ui.DietDetailItemView
import com.dearmyhealth.modules.Diet.viewmodel.DietTodayViewModel


class DietTodayFragment : Fragment() {

    private lateinit var binding: FragmentDietTodayBinding
    private lateinit var viewModel: DietTodayViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDietTodayBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this, DietTodayViewModel.Factory(
            requireActivity().application,
            this
        ))[DietTodayViewModel::class.java]


        observeViewModel()
        return binding.root
    }

    fun observeViewModel() {
        viewModel.todayDiets.observe(viewLifecycleOwner) { diets ->
            val dietlist = binding.todayDietListLL
            dietlist.removeAllViews()
            for( diet in diets ) {
                val view = DietDetailItemView(requireContext())
                view.setDiet(diet)
                dietlist.addView(view)
            }
        }
    }

    fun setNutrients(nuts: Nutrients) {

    }

}