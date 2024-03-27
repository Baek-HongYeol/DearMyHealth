package com.dearmyhealth.modules.exercise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.modules.exercise.model.ExerciseType

class ExerciseChooseViewModel: ViewModel() {

    val supportedExerciseType: LiveData<Array<ExerciseType>> = MutableLiveData(ExerciseType.values())
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

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