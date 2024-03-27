package com.dearmyhealth.modules.exercise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.modules.exercise.model.Exercise
import com.dearmyhealth.modules.exercise.model.ExerciseType

class ExerciseChooseViewModel: ViewModel() {

    val supportedExerciseType: LiveData<Array<ExerciseType>> = MutableLiveData(ExerciseType.values())
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val selectedExercise: MutableLiveData<ExerciseType?> = MutableLiveData(Exercise.currentExerciseType)


    fun setExercise() {
        selectedExercise.value = supportedExerciseType.value?.get(selectedPosition.value!!)
        Exercise.currentExerciseType = selectedExercise.value
    }

    fun clearExercise() {
        selectedExercise.value = null
        Exercise.currentExerciseType = null
    }


    class Factory(private val application: Application): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(ExerciseChooseViewModel::class.java)) {

                return ExerciseChooseViewModel(
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}