package com.jeremyhahn.cropdroid.data

import android.util.Log
import com.jeremyhahn.cropdroid.data.model.CropDroidAPI
import com.jeremyhahn.cropdroid.data.model.LoggedInUser
import okhttp3.*
import java.io.IOException


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(cropdroid: CropDroidAPI, username: String, password: String, callback: Callback): Result<LoggedInUser> {

        try {
            cropdroid.login(username, password, callback)
        } catch (e: Throwable) {
            return Result.Error(IOException("LoginDataSource: Error logging in: ", e))
        }

        return Result.Error(IOException("LoginDataSource: Error logging in:"))
    }

    fun logout() {
        // TODO: revoke authentication
    }



}

