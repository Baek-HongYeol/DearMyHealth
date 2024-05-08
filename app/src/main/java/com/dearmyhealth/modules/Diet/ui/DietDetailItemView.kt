package com.dearmyhealth.modules.Diet.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.databinding.ViewDietDetailItemBinding
import com.dearmyhealth.modules.Diet.model.Nutrients
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class DietDetailItemView(context: Context,
                         _binding: ViewDietDetailItemBinding? = null,
                         attrs: AttributeSet? = null,
                         defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, null, attrs, defStyleAttr)

    private var binding: ViewDietDetailItemBinding


    // 생성된 view가 있으면 해당 view의 binding을 전달받고 없으면 view를 생성.
    init {
        binding = _binding
            ?: ViewDietDetailItemBinding.inflate(
                LayoutInflater.from(context), this, true
            )
        // 이미지 정사각형으로 만들기
        val imageView = binding.dietImageIV
        val params = imageView.layoutParams
        params.width = imageView.layoutParams.height
        imageView.layoutParams = params
    }

    fun setDiet(diet: Diet) {
        setName(diet.name, diet.amount)
        setType(diet.type)
        setTime(diet.time)
        val nuts = Nutrients(0,0)
        nuts.calories = diet.calories
        nuts.nutrients.putAll( mutableMapOf(
            Nutrients.Names.carbohydrate to
                    diet.carbohydrate,
            Nutrients.Names.protein to
                    diet.protein,
            Nutrients.Names.fat to
                    diet.fat,
            Nutrients.Names.cholesterol to
                    diet.cholesterol
        ))
        generateNutrientChips(Nutrients(diet.user, diet.time, diet.calories, nuts.nutrients))
    }


    @SuppressLint("SetTextI18n")
    fun setName(name: String?, amount: Float) {
        val am = "%d".format(amount.roundToInt())
        if(name == null)
            binding.dietFoodTV.text = resources.getString(R.string.default_diet_description)
        else
            binding.dietFoodTV.text = "${am}x $name"
    }

    fun setName(name: String?) {
        binding.dietFoodTV.text = name ?: resources.getString(R.string.default_diet_description)
    }

    fun setType(type: Diet.MealType) {
        binding.dietNameTV.text = type.displayName
    }

    fun setTime(calendar: Calendar) {
        val sdf = SimpleDateFormat("a HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val time = sdf.format(calendar.time)
        binding.dietTimeTV.text = time
    }

    fun setTime(millis: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        setTime(cal)
    }

    @SuppressLint("SetTextI18n")
    fun generateNutrientChips(nuts: Nutrients?, amount: Float = 1f) {
        // 영양소를 각각 하나의 chip으로 표현
        val chips = binding.chipGroup
        chips.removeAllViews()


        // default 칩 추가
        var chip = makeChip()
        chip.text = resources.getString(R.string.calory_chip, 0)
        chips.addView(chip)
        if (nuts == null) return

        chip.text = resources.getString(R.string.calory_chip, ((nuts.calories?:0.0) * amount).toInt())
        for (nut in nuts.nutrients.entries) {
            if(nut.value == null)
                continue

            chip = makeChip()
            chip.text = nut.key.displayedName + ": ${(nut.value!!*amount).toInt()}g"
            chips.addView(chip)
        }
    }

    private fun makeChip(): Chip {
        val chipBackgroundDrawable = ResourcesCompat.getDrawable(resources,
            R.drawable.shape_radius_square_stroke_r16dp, context.theme)
        val chipBackgroundColor = ColorStateList.valueOf(resources.getColor(R.color.secondary, context.theme))

        val chip = Chip(context)
        chip.isClickable = false
        chip.backgroundDrawable = chipBackgroundDrawable
        chip.chipBackgroundColor = chipBackgroundColor
        chip.textStartPadding = 6f
        chip.textEndPadding = 6f
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)

        return chip
    }



}