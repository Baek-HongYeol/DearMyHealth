package com.dearmyhealth.data

import com.dearmyhealth.data.db.dao.UserDao
import com.dearmyhealth.data.db.entities.User.Gender
import com.dearmyhealth.data.model.LoggedInUser
import com.dearmyhealth.data.model.Session
import java.io.IOException
import java.lang.Exception

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: UserDao) {

    fun logout(user:LoggedInUser? = Session.currentUser) {
        if(user != null) Session.logout(user)
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        lateinit var result: Result<LoggedInUser>
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(0, "", "Jane Doe", 20, Gender.MALE)
            val target = dataSource.findByUserId(username).value
            result =
                if(target?.password == password)
                    Result.Success(LoggedInUser.from(target))
                else
                    Result.Error(Exception("Failed to login"))
        } catch (e: Throwable) {
            result = Result.Error(IOException("Error logging in", e))
        }

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        Session.login(loggedInUser)
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}