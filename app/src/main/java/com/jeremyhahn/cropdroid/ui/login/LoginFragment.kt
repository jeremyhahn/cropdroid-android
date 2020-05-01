package com.jeremyhahn.cropdroid.ui.login

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.User
import com.jeremyhahn.cropdroid.utils.ConfigManager
import com.jeremyhahn.cropdroid.utils.ConfigParser
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import com.jeremyhahn.cropdroid.utils.Preferences
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.lang.Thread.sleep


class LoginFragment() : Fragment() {

    private lateinit var repository: MasterControllerRepository
    private lateinit var preferences: Preferences
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var controller: MasterController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        repository = MasterControllerRepository(activity!!.applicationContext)

        var fragmentView = inflater.inflate(R.layout.fragment_login, container, false)

        preferences = Preferences(activity!!.applicationContext)

        controller = MasterController(
            arguments!!.getInt("controller_id", 0),
            0,
            arguments!!.getString("controller_name"),
            arguments!!.getString("controller_hostname"),
            0,
            0,
            "")

        val username = fragmentView.findViewById<EditText>(R.id.username)
        val password = fragmentView.findViewById<EditText>(R.id.password)
        val useSSL = fragmentView.findViewById<CheckBox>(R.id.useSSL)
        val login = fragmentView.findViewById<Button>(R.id.login)
        val loading = fragmentView.findViewById<ProgressBar>(R.id.loading)

        Log.d("LoginActivity.onCreate", "controller: " + controller!!.toString())

        try {
            val selectedController = repository.getController(controller!!.id)

            Log.d("LoginActivity.onCreate", "selectedController: " + selectedController.name)

            if(selectedController != null && !selectedController.token.isEmpty()) {
                var userid = JsonWebToken(activity!!.applicationContext).parse(selectedController.token).body.get("id").toString()
                var user = User(
                    userid,
                    username.text.toString(),
                    password.text.toString(),
                    selectedController.token,
                    "",
                    ""
                )
                updateUiWithUser(user, selectedController)
                return fragmentView
            }
        }
        catch(e: ExpiredJwtException) {
            Log.d("LoginActivity.onCreate", "JWT expired")
            controller!!.token = ""
            return fragmentView
        }

        for(controller in repository.allControllers) {
            Log.d("LoginActivity.onCreate", "registered controller: " + controller!!.toString())
        }

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory()).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginFragment, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginFragment, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            //setResult(Activity.RESULT_OK)

            if (loginResult.error!= null) {
                showLoginFailed(loginResult.error)
            }
            if(loginResult.registered) {
                loginViewModel.login(
                    CropDroidAPI(controller!!),
                    username.text.toString(),
                    password.text.toString())
            }

            if(loginResult.success != null) {

                var user = loginResult.success

                Log.d("LoginActivity token:", user.token)

                var jws = JsonWebToken(activity!!.applicationContext).parse(user.token)
                Log.d("jws", jws.toString())

                user.id = jws.body.get("id").toString()
                user.orgId = jws.body.get("organizationId").toString()

                controller!!.serverId = Integer.parseInt(jws.body.get("controllerId").toString())
                controller!!.secure = if(useSSL.isChecked) 1 else 0
                controller!!.userid = Integer.parseInt(user.id)
                controller!!.token = user.token

                var rowsUpdated = repository.updateController(controller!!)
                if (rowsUpdated != 1) {
                    Log.e(
                        "LoginActivity.loginResult.token",
                        "Unexpected number of rows effected: " + rowsUpdated.toString()
                    )
                    return@Observer
                }
                Log.i("LoginActivity.loginResult.token","Controller successfully authenticated")

                preferences.setController(controller!!, user)

                updateUiWithUser(user)
                //finish()
            }
        })
/*
        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }
*/
        password.apply {

            /*
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }*/

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            CropDroidAPI(controller!!),
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                checkSslFlag()
                loginViewModel.login(
                    CropDroidAPI(controller!!),
                    username.text.toString(),
                    password.text.toString()
                )
            }
        }

        return fragmentView
    }

    fun onRegister(v: View) {
        checkSslFlag()
        loginViewModel.register(
            CropDroidAPI(controller!!),
            username.text.toString(),
            password.text.toString()
        )
    }

    fun checkSslFlag() {
        if(useSSL.isChecked()) {
            controller!!.secure = 1
        } else {
            controller!!.secure = 0
        }
    }

    private fun updateUiWithUser(user: User, controller: MasterController = this.controller!!) {
        var haveConfig = false
        CropDroidAPI(controller!!).getConfig(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(":LoginActivity.getConfig.onFailure", "onFailure response: " + e!!.message)
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()
                Log.d("LoginActivity.getConfig.onResponse", "responseBody: " + responseBody)

                ConfigManager(
                    preferences.getControllerPreferences(),
                    ConfigParser.parse(responseBody)).sync()

                //val notificationServiceIntent = Intent(this, NotificationService::class.java)
                //notificationServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                //startForegroundService(notificationServiceIntent)

                haveConfig = true
                /*
                activity!!.runOnUiThread(Runnable {
                    (activity as MainActivity).navigateToMicrocontroller()
                })*/
            }
        })
        val progress = ProgressDialog(activity)
        progress.setTitle("Configuration")
        progress.setMessage("Loading...")
        progress.setCancelable(false)
        progress.show()

        while(haveConfig == false) {
            sleep(200)
            Log.d("LoginFragment", "Waiting 200ms for response from web server...")
        }

        progress.dismiss()
        (activity as MainActivity).navigateToMicrocontroller()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(activity!!.applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(activity!!.applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
/*
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
*/