package com.dearmyhealth.Temp

import android.content.Context
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import android.app.Activity

import android.widget.TextView

class ExerciseButton {
    companion object {
        var lastSelectedText: String? = null

        fun setupLayoutClickListener(context: Context, vararg layoutIds: Pair<Int, Int>) {
            layoutIds.forEach { pair ->
                val layoutId = pair.first
                val textViewId = pair.second
                val layout = (context as Activity).findViewById<ConstraintLayout>(layoutId)
                val textView = layout.findViewById<TextView>(textViewId)

                layout.setOnClickListener {
                    lastSelectedText = textView.text.toString()
                    Toast.makeText(context, lastSelectedText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
