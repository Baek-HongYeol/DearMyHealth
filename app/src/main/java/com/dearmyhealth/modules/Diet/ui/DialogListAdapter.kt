package com.dearmyhealth.modules.Diet.ui

import android.app.ActionBar.LayoutParams
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Food

data class DialogListItem(
    val food: Food
)

class FoodHolder(
    var view: FoodItem
) : RecyclerView.ViewHolder(view)

class DialogListAdapter(
    var list: ArrayList<DialogListItem>,
    val listener: OnClickListener?
) : RecyclerView.Adapter<FoodHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        val view = FoodItem(parent.context)
        val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.layoutParams = lp
        return FoodHolder(view)
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        val item = list[position]
        val view = holder.view

        view.apply {
            title = item.food.식품명
            entpName = item.food.제조사
            weight = (if(item.food.내용량_단위 == "mL") item.food.총내용량mL else  item.food.총내용량g) + item.food.내용량_단위
            calories = "${item.food.에너지kcal}kcal"
            listener.apply { setOnClickListener(listener) }
        }
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount(): Int = list.size

}