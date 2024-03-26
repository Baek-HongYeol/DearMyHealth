package com.dearmyhealth.modules.exercise.model

enum class ExerciseType(val id: Int) {
    WALKING(79),
    RUNNING(55),
    HIKING(37),
    BIKING(8),
    SWIMMING(74),
    UNKNOWN(0)
}

class Excercise {

    companion object {
        val currentExerciseType: ExerciseType? = null


    }

}