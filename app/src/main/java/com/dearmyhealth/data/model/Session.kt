package com.dearmyhealth.data.model

import com.dearmyhealth.data.db.dao.UserDao

object Session {

    private var users : MutableList<LoggedInUser> = mutableListOf()
    private var _currentUser: LoggedInUser? = null
    val currentUser: LoggedInUser? get() = _currentUser
    lateinit var dao: UserDao

    fun checkExpire():Boolean {
        var expired : MutableList<LoggedInUser> = mutableListOf()
        var ret = false
        users.forEach { user ->
            if(user.expireDate < System.currentTimeMillis()){
                expired.add(user)
                ret = true
            }
            else if(dao.findByUserId(user.userid).value == null){
                expired.add(user)
                ret = true
            }

        }
        expired.forEach { user ->
            users.remove(user)
            if(_currentUser == user)
                _currentUser = null
        }
        return ret
    }

    fun login(user:LoggedInUser) {
        users.add(user)
        _currentUser = user
    }

    fun logout(user:LoggedInUser) {
        users.remove(user)
        if(_currentUser == user && users.size > 0)
            _currentUser = users[0]
    }
}