package com.dearmyhealth.modules.Diet.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.databinding.FragmentDietTodayBinding
import com.dearmyhealth.modules.Diet.model.Nutrients
import com.dearmyhealth.modules.Diet.ui.DietListAdapter
import com.dearmyhealth.modules.Diet.ui.NutritionStandardReferenceView
import com.dearmyhealth.modules.Diet.viewmodel.DietTodayViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.round


class DietTodayFragment : Fragment() {

    private lateinit var binding: FragmentDietTodayBinding
    private lateinit var viewModel: DietTodayViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDietTodayBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this, DietTodayViewModel.Factory(
            requireActivity().application
        ))[DietTodayViewModel::class.java]

        binding.todayDietListRV.adapter = DietListAdapter(listOf())
        binding.todayDietListRV.layoutManager = LinearLayoutManager(context)

        binding.todayDietHelpTV.setOnClickListener {
            val view = NutritionStandardReferenceView(requireContext())
            AlertDialog.Builder(requireContext())
                .setView(view)
                .show()
        }

        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        viewModel.todayDiets.observe(viewLifecycleOwner) { diets ->
            viewModel.calcNutrients()
            val adapter = binding.todayDietListRV.adapter as DietListAdapter
            val sortedDiets = diets.sortedBy { diet: Diet -> diet.time }
            adapter.setData(sortedDiets)
        }

        viewModel.currentQueryDateString.observe(viewLifecycleOwner) { dateStr ->
            binding.currentDateTextView.text = dateStr
        }

        viewModel.todayNutritions.observe(viewLifecycleOwner) { nuts ->
            setNutrientStats(nuts)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setNutrientStats(nuts: Nutrients) {
        binding.todayDietCaloriesValueTV.text = "${nuts.calories}kcal"

        val carbo = nuts.nutrients[Nutrients.Names.carbohydrate] ?: 0.0
        val protein = nuts.nutrients[Nutrients.Names.protein] ?: 0.0
        val fat = nuts.nutrients[Nutrients.Names.fat] ?: 0.0

        binding.todayDietCarboValueTV.text = "${carbo}g"
        binding.todayDietProteinValueTV.text = "${protein}g"
        binding.todayDietFatValueTV.text = "${fat}g"

        val sum = carbo + protein + fat
        val carboR = round((carbo*100)/sum)/10
        val proteinR = round((protein*100)/sum)/10
        val fatR = round((fat*100)/sum)/10
        binding.todayDietNutrientsRatioTV.text = "${carboR}:${proteinR}:${fatR}"
        var temp: LayoutParams = binding.todayDietCarboBar.layoutParams as LayoutParams
        temp.weight = carboR.toFloat()
        binding.todayDietCarboBar.layoutParams = temp
        temp = binding.todayDietProteinBar.layoutParams as LayoutParams
        temp.weight = proteinR.toFloat()
        (binding.todayDietProteinBar.layoutParams) = temp
        temp = binding.todayDietFatBar.layoutParams as LayoutParams
        temp.weight = fatR.toFloat()
        (binding.todayDietFatBar.layoutParams) = temp
        (binding.todayNutrientBar.invalidate())
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            121 -> CoroutineScope(Dispatchers.IO).launch { viewModel.deleteDiet(item.groupId) }
        }
        return true
    }

}