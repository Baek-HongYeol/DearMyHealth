package com.dearmyhealth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    private val _isLooggedin = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLooggedin

}