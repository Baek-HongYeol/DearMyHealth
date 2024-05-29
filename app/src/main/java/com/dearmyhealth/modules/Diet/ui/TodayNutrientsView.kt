package com.dearmyhealth.modules.Diet.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.dearmyhealth.data.db.entities.User
import com.dearmyhealth.databinding.ViewDietTodayNutritionsBinding
import com.dearmyhealth.modules.Diet.model.Nutrients
import com.dearmyhealth.modules.login.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodayNutrientsView(context: Context, view: View?=null) : ConstraintLayout(context) {

    private var binding: ViewDietTodayNutritionsBinding
    private lateinit var RNI: Nutrients
    var isSimpleView: Boolean = false

    init {
        @Suppress("LiftReturnOrAssignment")
        if(view == null)
            binding = ViewDietTodayNutritionsBinding.inflate(LayoutInflater.from(context))
        else
            binding = ViewDietTodayNutritionsBinding.bind(view)
    }



    @SuppressLint("SetTextI18n")
    fun setNutrients(nutrients: Nutrients) {
        binding.nutrientsLL.removeAllViews()
        val names = nutrients.getPresentNutrientsNames()
        while(binding.nutrientsLL.childCount < names.size) {
            binding.nutrientsLL.addView(TodayNutritionItemView(context))
        }
        binding.todayCalories.text = nutrients.calories.toString()
        CoroutineScope(Dispatchers.IO).launch {
            RNI = Nutrients.loadNutrientRNI(Session.currentUser.gender?: User.Gender.MALE, Session.currentUser.age)
            withContext(Dispatchers.Main) {
                binding.suggestedCalories.text = "/${RNI.calories?.toInt() ?: 0} kcal"
                var i = 0
                for( v in binding.nutrientsLL.children) {
                    (v as TodayNutritionItemView)
                        .setChart(
                            getNutrientString(names[i]),
                            nutrients.getNutrientValueByName(names[i])?.toFloat() ?: 0f,
                            RNI.getNutrientValueByName(names[i])?.toInt() ?: 0,
                            isSimpleView
                        )
                    i++
                }
            }
        }
    }


    private fun getNutrientString(name: String) : String {
        val resId = Nutrients.Names.valueOf(name).displayedName

        return if (resId != null) ContextCompat.getString(context, resId)
        else name
    }
}