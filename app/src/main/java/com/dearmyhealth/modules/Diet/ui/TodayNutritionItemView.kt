package com.dearmyhealth.modules.Diet.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.dearmyhealth.R
import com.dearmyhealth.databinding.ViewTodayNutritionItemBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class TodayNutritionItemView(
    @get:JvmName("getAdapterContext") val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {

    }

    private val binding = ViewTodayNutritionItemBinding.inflate(
        LayoutInflater.from(context),this, true
    )
    init{
        val shapeDrawable = binding.barChart.background as ShapeDrawable?
        shapeDrawable?.paint?.color = ContextCompat.getColor(context, R.color.secondary)
    }

    fun setChart(name:String, value:Float, suggestion:Int) {
        binding.name = name
        binding.value = value
        binding.suggested = suggestion

        val entries = ArrayList<BarEntry>() // entries를 선언
        entries.add(BarEntry(0f, value.toFloat()))
        var set1 = BarDataSet(entries, "DataSet 1")
        set1.color = Color.GREEN // Bar의 컬러
        set1.highLightColor = resources.getColor(R.color.primary) // highLight의 컬러
        set1.highLightAlpha = 255 // highLight의 투명도
        set1.formLineWidth = 10f
        binding.barChart.run {
            setDrawBarShadow(true) // 그래프 그림자
            setTouchEnabled(false) // 차트 터치 막기
            setMaxVisibleValueCount(1) // 그래프 최대 갯수
            legend.isEnabled = false // 차트 범례 설정(legend object chart)
            description.isEnabled = false
            xAxis.run { // 아래 라벨 x축
                isEnabled = false // 라벨 표시 설정
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)

            }
            axisLeft.run {
                isEnabled = true
                axisMinimum = 0f
                axisMaximum = if (suggestion < value) value.toFloat() else suggestion.toFloat()
            }
            axisRight.run {
                isEnabled = false
            }
        }
        binding.barChart.data = BarData(BarDataSet(entries, "NutData"))
    }

}