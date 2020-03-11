package com.jeremyhahn.cropdroid.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.MicroControllerActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.User
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.utils.ConfigManager
import com.jeremyhahn.cropdroid.utils.ConfigParser
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    var controller : MasterController? = null
    val repository: MasterControllerRepository

    init {
        this.repository = MasterControllerRepository(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val useSSL = findViewById<CheckBox>(R.id.useSSL)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        controller = MasterController(
            intent.getIntExtra("controller_id", 0),
            intent.getIntExtra("controller_server_id", 0),
            intent.getStringExtra("controller_name"),
            intent.getStringExtra("controller_hostname"),
            0,
            0,
            "")

        Log.d("LoginActivity.onCreate", "controller: " + controller!!.toString())

        try {
            val selectedController = repository.getController(controller!!.id)

            Log.d("LoginActivity.onCreate", "selectedController: " + selectedController.name)

            if(selectedController != null && !selectedController.token.isEmpty()) {
                var userid = JsonWebToken(this).parse(selectedController.token).body.get("id").toString()
                var user = User(
                    userid,
                    username.text.toString(),
                    password.text.toString(),
                    selectedController.token,
                    "",
                    ""
                )
                updateUiWithUser(user, selectedController)
                return
            }
        }
        catch(e: ExpiredJwtException) {
            Log.d("LoginActivity.onCreate", "JWT expired")
            controller!!.token = ""
            return
        }

        for(controller in repository.allControllers) {
            Log.d("LoginActivity.onCreate", "registered controller: " + controller!!.toString())
        }

        Log.d("LoginActivity.onCreate: controller_id", "" + controller!!.id)
        Log.d("LoginActivity.onCreate: controller_server_id", "" + controller!!.serverId)
        Log.d("LoginActivity.onCreate: controller_name", "" + controller!!.name)
        Log.d("LoginActivity.onCreate: controller_hostname", controller!!.hostname)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory()).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
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

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            setResult(Activity.RESULT_OK)

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

                var jws = JsonWebToken(this).parse(user.token)
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

                val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = prefs.edit()
                editor.putInt(PREF_KEY_CONTROLLER_ID, controller!!.id)              //  local sqlite controller id
                editor.putInt("controller_server_id", controller!!.serverId) // remote database controller id
                editor.putString("controller_name", controller!!.name)
                editor.putString("controller_hostname", controller!!.hostname)
                editor.putString("user_id", user.id)
                editor.putString("jwt", user.token)
                if(!editor.commit()) {
                    Log.e("LoginActivity", "Error committing defaultSharedPreferences")
                }

                updateUiWithUser(user)
                finish()
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

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

        val notificationServiceIntent = Intent(this, NotificationService::class.java)
        notificationServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val microControllerActivity = Intent(this, MicroControllerActivity::class.java)

        CropDroidAPI(controller!!).getConfig(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(":LoginActivity.getConfig.onFailure", "onFailure response: " + e!!.message)
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()
                Log.d("LoginActivity.getConfig.onResponse", "responseBody: " + responseBody)

                ConfigManager(
                    PreferenceManager.getDefaultSharedPreferences(applicationContext),
                    ConfigParser.parse(responseBody)).sync()

                startForegroundService(notificationServiceIntent)
                startActivity(microControllerActivity)
            }
        })
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
