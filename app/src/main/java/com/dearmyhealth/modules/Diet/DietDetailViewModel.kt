package com.dearmyhealth.modules.Diet

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase

class DietDetailViewModel : ViewModel() {



    class Factory(val context: Context, private val lifecycleOwner: LifecycleOwner): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietDetailViewModel::class.java)) {
                val foodDao = AppDatabase.getDatabase(context).foodDao()
                val dietDao = AppDatabase.getDatabase(context).dietDao()
                return DietDetailViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}