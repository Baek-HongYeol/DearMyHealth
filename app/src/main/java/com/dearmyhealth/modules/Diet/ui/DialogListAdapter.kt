package com.dearmyhealth.modules.Diet.ui

import android.app.ActionBar.LayoutParams
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Food


class FoodHolder(
    var view: FoodItem,
    val listener: (View, Int) -> Unit
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener { v->
            if (adapterPosition != RecyclerView.NO_POSITION)
                listener(v, adapterPosition)
        }
    }
}

class DialogListAdapter(
    var list: List<Food>,
    val listener: (View, Int) -> Unit
) : RecyclerView.Adapter<FoodHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        val view = FoodItem(parent.context)
        val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.layoutParams = lp
        Log.d("DialogListAdapter", "create view holder!")
        return FoodHolder(view, listener)
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        val item = list[position]
        val view = holder.view

        view.apply {
            category = item.식품대분류
            title = item.식품명
            entpName = item.제조사
            weightUnit = item.내용량_단위 ?: ""
            weight = if(item.내용량_단위 == "mL") item.총내용량mL  else item.총내용량g
            if(weight == "-")
                weight = "${item.onetime제공량}"
            onePackWeight = if(weight != "${item.onetime제공량}")  "${item.onetime제공량}" else null
            calories = "${item.에너지kcal}kcal"
        }
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount(): Int = list.size

}