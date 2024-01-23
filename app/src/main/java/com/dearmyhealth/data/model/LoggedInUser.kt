package com.dearmyhealth.data.model

import com.dearmyhealth.data.LoginRepository
import com.dearmyhealth.data.db.entities.User
import com.dearmyhealth.data.db.entities.User.Gender


/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */

data class LoggedInUser(
    val uid: Int,
    val userid: String,
    val displayName: String,
    val age: Int,
    val gender: Gender?,
    val authenticator: LoginRepository?=null,
    var expireDate: Long = Long.MAX_VALUE
) {
    companion object {
        fun from(user: User): LoggedInUser{
            return LoggedInUser(user.uid, user.userid, user.name, user.age, user.gender)
        }
    }
}