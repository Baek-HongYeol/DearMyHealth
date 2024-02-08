package com.dearmyhealth.activities.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.dearmyhealth.databinding.ViewTodayNutritionItemBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class TodayNutritionItemView(
    @get:JvmName("getAdapterContext") val context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTodayNutritionItemBinding.inflate(
        LayoutInflater.from(context),this, true
    )

    fun setChart(name:String, value:Int, suggestion:Int) {
        binding.name = name
        binding.value = value
        binding.suggested = suggestion

        val entries = ArrayList<BarEntry>() // entries를 선언
        entries.add(BarEntry(0f, value.toFloat()))
        var set1 = BarDataSet(entries, "DataSet 1")
        set1.color = Color.GREEN // Bar의 컬러
        set1.highLightColor = Color.parseColor("#009900") // highLight의 컬러
        set1.highLightAlpha = 255 // highLight의 투명도
        binding.barChart.run {
            setDrawBarShadow(true) // 그래프 그림자
            setTouchEnabled(false) // 차트 터치 막기
            setMaxVisibleValueCount(1) // 그래프 최대 갯수
            legend.isEnabled = false // 차트 범례 설정(legend object chart)

            xAxis.run { // 아래 라벨 x축
                isEnabled = false // 라벨 표시 설정
                axisMinimum = 0f
                axisMaximum = if (suggestion < value) value.toFloat() else suggestion.toFloat()

            }
        }
        binding.barChart.data = BarData(BarDataSet(entries, "NutData"))
    }

}