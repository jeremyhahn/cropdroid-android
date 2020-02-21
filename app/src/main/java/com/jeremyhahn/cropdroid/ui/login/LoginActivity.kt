package com.jeremyhahn.cropdroid.ui.login

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
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
import com.jeremyhahn.cropdroid.GLOBAL_PREFS
import com.jeremyhahn.cropdroid.MicroControllerActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.WebSocketActivity
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.User
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import kotlinx.android.synthetic.main.activity_login.*
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    var controllerHostname : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val controllerId = intent.getStringExtra("controller_id")
        val controllerName = intent.getStringExtra("controller_name")
        controllerHostname = intent.getStringExtra("controller_hostname")

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val useSSL = findViewById<CheckBox>(R.id.useSSL)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        var repo = MasterControllerRepository(this)
        val selectedController = repo.getControllerByHostname(controllerHostname!!)
        if(!selectedController!!.token.isNullOrEmpty()) {
            try {
                var userid =
                    JsonWebToken(this).parse(selectedController.token).body.get("id").toString()
                var user = User(
                    userid,
                    username.text.toString(),
                    password.text.toString(),
                    selectedController.token
                )
                updateUiWithUser(user, selectedController, true)
                return
            }
            catch(e: ExpiredJwtException) {
                Log.d("LoginActivity.onCreate", "JWT expired")
                selectedController.token = ""
            }
        }
        for(controller in repo.allControllers) {
            Log.d("LoginActivity.onCreate", "registered controller: " + controller.toString())
        }
        //repo.drop()

        Log.d("LoginActivity.onCreate: controller_id", controllerId)
        Log.d("LoginActivity.onCreate: controller_name", controllerName)
        Log.d("LoginActivity.onCreate: controller_hostname", controllerHostname!!)

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
                    CropDroidAPI(
                        controllerHostname!!,
                        useSSL.isChecked
                    ),
                    username.text.toString(),
                    password.text.toString()
                )
            }

            if(loginResult.success != null) {

                var user = loginResult.success!!

                val prefs = getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("jwt_token", user.token)
                val commit = editor.commit()

                /*
                var publicKey: PublicKey? = null
                val am: AssetManager = getAssets()
                val jwtIs: InputStream = am.open("rsa.pub.der")
                val spec = X509EncodedKeySpec(IOUtils.toByteArray(jwtIs))
                val kf = KeyFactory.getInstance("RSA")
                publicKey = kf.generatePublic(spec)

                var jws = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(user.token)
                */

                var jws = JsonWebToken(this).parse(user.token)
                Log.d("jws", jws.toString())

                user.id = jws.body.get("id").toString()

                var authenticatedController = MasterController(Integer.parseInt(controllerId), controllerName, controllerHostname!!, user.token)
                var rowsUpdated = MasterControllerRepository(this).updateController(authenticatedController)
                if(rowsUpdated != 1) {
                    Log.e("LoginActivity.loginResult.token", "Unexpected number of rows effected: " + rowsUpdated.toString())
                }
                else {
                    Log.i("LoginActivity.loginResult.token", "Controller successfully authenticated")
                    updateUiWithUser(user, authenticatedController)
                    finish()
                }
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
                            CropDroidAPI(
                                controllerHostname!!,
                                useSSL.isChecked
                            ),
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(
                    CropDroidAPI(
                        controllerHostname!!,
                        useSSL.isChecked
                    ),
                    username.text.toString(),
                    password.text.toString()
                )
            }
        }
    }

    fun onRegister(v: View) {
        loginViewModel.register(
            CropDroidAPI(
                controllerHostname!!,
                useSSL.isChecked
            ),
            username.text.toString(),
            password.text.toString()
        )
    }

    private fun updateUiWithUser(user: User, controller: MasterController, alreadyLoggedIn: Boolean = false) {
        val welcome = getString(R.string.welcome)
        val displayName = user.username

  //      if(!alreadyLoggedIn) {
            var intent = Intent(this, NotificationService::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("user_id", user.id)
            intent.putExtra("controller_id", controller.id)
            intent.putExtra("controller_hostname", controller.hostname)
            intent.putExtra("jwt", controller.token)
            startService(intent)
//        }

        startActivity(Intent(this, MicroControllerActivity::class.java))

        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
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
