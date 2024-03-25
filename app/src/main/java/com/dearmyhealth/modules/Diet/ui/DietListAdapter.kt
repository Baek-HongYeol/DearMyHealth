package com.dearmyhealth.modules.Diet.ui

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.data.db.entities.Diet


class DietHolder(
    var view: DietDetailItemView,
    val listener: ((View, Int) -> Unit)?
) : RecyclerView.ViewHolder(view) {
    init {
        if (listener != null)
            view.setOnClickListener { v->
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.invoke(v, adapterPosition)
            }
    }
}

class DietListAdapter(
    var list: List<Diet>,
    val listener: ((View, Int) -> Unit)? = null
) : RecyclerView.Adapter<DietHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietHolder {
        val view = DietDetailItemView(parent.context)
        val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.layoutParams = lp
        return DietHolder(view, listener)
    }

    override fun onBindViewHolder(holder: DietHolder, position: Int) {
        val item = list[position]
        val view = holder.view

        view.setDiet(item)
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Diet>) {
        this.list = list
        notifyDataSetChanged()
    }

}