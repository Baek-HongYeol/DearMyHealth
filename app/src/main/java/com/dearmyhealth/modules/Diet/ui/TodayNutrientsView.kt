package com.dearmyhealth.modules.Diet.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.dearmyhealth.databinding.ViewDietTodayNutritionsBinding
import com.dearmyhealth.modules.Diet.model.Nutrients

class TodayNutrientsView(context: Context, view: View?=null) : ConstraintLayout(context) {

    private lateinit var binding: ViewDietTodayNutritionsBinding

    init {
        @Suppress("LiftReturnOrAssignment")
        if(view == null)
            binding = ViewDietTodayNutritionsBinding.inflate(LayoutInflater.from(context))
        else
            binding = ViewDietTodayNutritionsBinding.bind(view)
    }



    fun setNutrients(nutrients: Nutrients) {
        binding.nutrientsLL.removeAllViews()
        val names = nutrients.getPresentNutrientsNames()
        while(binding.nutrientsLL.childCount < names.size) {
            binding.nutrientsLL.addView(TodayNutritionItemView(context))
        }
        binding.todayCalories.text = nutrients.calories.toString()

        var i = 0
        for( v in binding.nutrientsLL.children) {
            (v as TodayNutritionItemView)
                .setChart(
                    getNutrientString(names[i]),
                    nutrients.getNutrientValueByName(names[i])?.toFloat() ?: 0f,
                    0
                )

            i++
        }
    }


    private fun getNutrientString(name: String) : String {
        val resId = Nutrients.resourceIds[name]

        return if (resId != null) ContextCompat.getString(context, resId)
        else name
    }
}