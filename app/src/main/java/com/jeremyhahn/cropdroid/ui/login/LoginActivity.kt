package com.jeremyhahn.cropdroid.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.jeremyhahn.cropdroid.MasterControllerListActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.model.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.android.synthetic.main.activity_login.*
import java.security.Key


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val server = findViewById<EditText>(R.id.server)
        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val useSSL = findViewById<CheckBox>(R.id.useSSL)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        var repo = MasterControllerRepository(this)
        for(controller in repo.allControllers) {
            Log.d("stored master controller", controller.toString())
        }
        //repo.drop()

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
            if (loginResult.message!= null) {
                showLoginFailed(loginResult.message)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                finish()
            }
            if(loginResult.registered) {
                loginViewModel.login(
                    CropDroidAPI(server.text.toString(), useSSL.isChecked),
                    username.text.toString(),
                    password.text.toString()
                )
            }
            if(loginResult.token != null) {
                val prefs = getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("token", loginResult.token)

                /*
                val key: Key = Keys.secretKeyFor(SignatureAlgorithm.RS256)
                var jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(loginResult.token)

                Log.d("jws", jws.toString())
                jws.body.get("Id")
                Log.d("Id", jws.body.get("Id").toString())
                */

                editor.commit()
                var persistedMaster = MasterControllerRepository(this).addController(
                    MasterController(0, server.text.toString(), server.text.toString(), loginResult.token)
                )
                Log.d("LoginActivity.persistedMaster", persistedMaster.toString())
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
                            CropDroidAPI(server.text.toString(), useSSL.isChecked),
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(
                    CropDroidAPI(server.text.toString(), useSSL.isChecked),
                    username.text.toString(),
                    password.text.toString()
                )
            }
        }
    }

    fun onRegister(v: View) {
        loginViewModel.register(
            CropDroidAPI(server.text.toString(), useSSL.isChecked),
            username.text.toString(),
            password.text.toString()
        )
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        startActivity(Intent(this, MasterControllerListActivity::class.java))
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
