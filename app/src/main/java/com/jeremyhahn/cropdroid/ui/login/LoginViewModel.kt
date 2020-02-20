package com.jeremyhahn.cropdroid.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginViewModel() : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(cropdroid: CropDroidAPI, username: String, password: String) {

        cropdroid.login(username, password, object : Callback {

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("LoginViewModel.login", "onFailure response: " + e!!.message)
                _loginResult.postValue(LoginResult(error = e!!.message))
                return
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d("LoginViewModel.login", "login response: " + response)

                var responseBody = response.body().string()
                Log.d("LoginViewModel.login", "responseBody: " + responseBody)

                var json = JSONObject(responseBody)
                if (!response.isSuccessful()) {
                    Log.d("LoginViewModel.login", "fail: " + responseBody)
                    _loginResult.postValue(LoginResult(error = json.getString("error")))
                    return
                }

                var user : LoggedInUserView? = LoggedInUserView(displayName = username)
                _loginResult.postValue(LoginResult(User("0", username, "", json.getString("token"))))
            }
        })
    }

    fun register(cropdroid: CropDroidAPI, username: String, password: String) {

        cropdroid.register(username, password, object : Callback {

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("LoginViewModel.register", "onFailure response: " + e!!.message)
                _loginResult.postValue(LoginResult(error = e!!.message))
                return
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d("LoginViewModel.register", "login response: " + response)

                var responseBody = response.body().string()
                var json = JSONObject(responseBody)

                if (!response.isSuccessful()) {
                    Log.d("LoginViewModel.register", "fail: " + responseBody)
                    _loginResult.postValue(LoginResult(error = json.getString("error")))
                    return
                }

                if(json.getBoolean("success")) {
                    _loginResult.postValue(LoginResult(registered = true))
                }

                _loginResult.postValue(LoginResult(error = "Unexpected error"))
            }
        })
    }


    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }


    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }


    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
