package com.dearmyhealth.modules.exercise.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.dearmyhealth.R

enum class ExerciseType(val id: Int, val displayName: String) {
    WALKING(79, "걷기"),
    RUNNING(55, "달리기"),
    HIKING(37, "등산"),
    BIKING(8, "자전거"),
    SWIMMING(74, "수영"),
    UNKNOWN(0, "UNKNOWN")
}

class Exercise {

    companion object {
        var currentExerciseType: ExerciseType? = null

        fun getDrawableOf(context: Context, type: ExerciseType): Drawable? {
            val resource = when(type) {
                ExerciseType.WALKING -> R.drawable.walk
                ExerciseType.RUNNING -> R.drawable.run
                ExerciseType.HIKING -> R.drawable.hiking
                ExerciseType.BIKING -> null
                ExerciseType.SWIMMING -> null
                ExerciseType.UNKNOWN -> null
            } ?: return null
            return ContextCompat.getDrawable(context, resource)
        }

    }

}