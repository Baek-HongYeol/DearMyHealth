package com.dearmyhealth.modules.Diet.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.dearmyhealth.databinding.ItemFoodBinding

class FoodItem(
    @get:JvmName("getAdapterContext") val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context,attrs,defStyleAttr) {
    private val binding: ItemFoodBinding =
        ItemFoodBinding.inflate(LayoutInflater.from(context), this, true)

    /** 분류.
     * 해당 식품의 대분류이다.
     */
    var category: String
        get() = binding.itemFoodCategory.text.toString()
        set(value) {
            binding.itemFoodCategory.text = value
        }
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

    // 총 내용량
    var weight: String = "0"
        set(value) {
            field = value
            weightString = value+weightUnit
        }

    /** 1회 제공량. 총 내용량이 없다면 1회 제공량이 곧 총 내용량이다.
     *  1회 제공량이 없거나 총 내용량 == 1회 제공량인 경우
     *  1회 제공량 view는 생략한다.
     */
    var onePackWeight: String? = null
        set(value) {
            field = value
            if(field == null) {
                binding.itemFoodOnePackWeight.visibility = View.GONE
                binding.itemFoodOnePackText.visibility = View.GONE
            }
            else {
                onePackWeightString = value + weightUnit
                binding.itemFoodOnePackWeight.visibility = View.VISIBLE
                binding.itemFoodOnePackText.visibility = View.VISIBLE
            }
        }
    // g 또는 mL
    var weightUnit: String = "g"
        set(value) {
            field = value
            weightString = weight+value
        }

    /** 내용량단위와 결합하여 만들어진 용량 표기
     *  weight와 weightUnit의 수정으로만 weightString이 수정된다.
     */
    var weightString: String
        get() = binding.itemFoodTotalWeight.text.toString()
        private set(value) {
            binding.itemFoodTotalWeight.text = value
        }

    /** 내용량단위와 결합하여 만들어진 1회제공량 표기
     *  onePackWeight와 weightUnit의 수정으로만 onePackWeightString이 수정된다.
     */
    var onePackWeightString: String
        get() = binding.itemFoodOnePackWeight.text.toString()
        private set(value) {
            binding.itemFoodOnePackWeight.text = value
        }

    var calories: String
        get() = binding.itemFoodCalory.text.toString()
        set(value) {
            binding.itemFoodCalory.text = value
        }
}