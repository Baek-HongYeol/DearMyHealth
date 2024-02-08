package com.dearmyhealth.activities.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.dearmyhealth.databinding.ItemFoodBinding

class FoodItem(
    @get:JvmName("getAdapterContext") val context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
): ConstraintLayout(context,attrs,defStyleAttr) {
    private val binding: ItemFoodBinding =
        ItemFoodBinding.inflate(LayoutInflater.from(context), this, true)

    var title: String
        get() = binding.itemFoodName.text.toString()
        set(value) {
            binding.itemFoodName.text = value
        }

    var entpName: String
        get() = binding.itemFoodEntpTV.text.toString()
        set(value) {
            binding.itemFoodEntpTV.text = value
        }

    var weight: String
        get() = binding.itemFoodWeight.text.toString()
        set(value) {
            binding.itemFoodWeight.text = value
        }

    var calories: String
        get() = binding.itemFoodCalory.text.toString()
        set(value) {
            binding.itemFoodCalory.text = value
        }
}