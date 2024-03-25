package com.dearmyhealth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dearmyhealth.modules.login.Session

class MainViewModel: ViewModel() {
    private val _isLooggedin = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLooggedin

    /**
     * checkSession() - 계정 상태의 변경을 확인한다.
     * 로그인 여부, 현재 로그인된 계정에 따라 데이터를 새로고침한다.
     */
    fun checkSession(){
        Session.checkExpire()
        _isLooggedin.value = Session.currentUser != null
        // 데이터 새로고침
    }

}