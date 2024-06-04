package com.dearmyhealth.modules.exercise.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.R
import com.dearmyhealth.databinding.ViewExerciseTypeBinding
import com.dearmyhealth.modules.exercise.model.ExerciseSession
import com.dearmyhealth.modules.exercise.model.ExerciseType


class ExerciseTypeHolder(
    var binding: ViewExerciseTypeBinding,
    val listener: ((View, Int) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    init {
        if (listener != null)
            binding.root.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION)
                    listener.invoke(binding.root, adapterPosition)
            }
    }

    fun defaultBg() {
        binding.root.backgroundTintList =  ColorStateList.valueOf(Color.TRANSPARENT)
    }
    fun selectedBg() {
        binding.root.backgroundTintList =  ColorStateList.valueOf(
            ContextCompat.getColor(binding.root.context, R.color.secondary)
        )
    }
}


class ExerciseTypeListAdapter(
    var list: List<ExerciseType>,
    val listener: ((View, Int) -> Unit)? = null
): RecyclerView.Adapter<ExerciseTypeHolder>() {
    var lastSelectedItem = -1
        private set
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseTypeHolder {
        val view = ViewExerciseTypeBinding.inflate(LayoutInflater.from(parent.context))
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.root.layoutParams = lp

        return ExerciseTypeHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ExerciseTypeHolder, position: Int) {
        val item = list[position]
        val binding = holder.binding

        binding.exerciseIconIV.setImageDrawable(ExerciseSession.getDrawableOf(binding.root.context, item))
        binding.exerciseNameTV.text = item.displayName

        if(position == lastSelectedItem)
            holder.selectedBg()
        else
            holder.defaultBg()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int) = position.toLong()

    fun setSelectedItem(position: Int) {
        notifyItemChanged(lastSelectedItem)
        lastSelectedItem = position
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<ExerciseType>) {
        this.list = list
        lastSelectedItem = -1
        notifyDataSetChanged()
    }
}