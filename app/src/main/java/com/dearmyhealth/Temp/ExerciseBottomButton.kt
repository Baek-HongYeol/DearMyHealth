package com.dearmyhealth.Temp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dearmyhealth.R


class ExerciseBottomButtonFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_exercise_bottom_button, container, false)

        val applyButton = view.findViewById<Button>(R.id.button_apply)
        applyButton.setOnClickListener {
            updateExerciseInfo()
        }

        return view
    }

    private fun updateExerciseInfo() {
        val currentExerciseTextView = activity?.findViewById<TextView>(R.id.exercise_now_is)
        val currentExerciseText = currentExerciseTextView?.text.toString()

        val selectedExerciseText = ExerciseButton.lastSelectedText

        if (selectedExerciseText != null && currentExerciseText != selectedExerciseText) {
            currentExerciseTextView?.text = selectedExerciseText
        }
    }
}
