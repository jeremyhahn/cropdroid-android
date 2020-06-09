package com.jeremyhahn.cropdroid.ui.login

import android.content.SharedPreferences
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
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_ID_KEY
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.ClientConfig
import com.jeremyhahn.cropdroid.config.ConfigManager
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import com.jeremyhahn.cropdroid.utils.Preferences
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.android.synthetic.main.activity_login.*

class LoginFragment() : Fragment() {

    private lateinit var repository: MasterControllerRepository
    private lateinit var preferences: Preferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var controller: ClientConfig

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val fragmentActivity = requireActivity()
        repository = MasterControllerRepository(fragmentActivity.applicationContext)

        var fragmentView = inflater.inflate(R.layout.fragment_login, container, false)
        val args = requireArguments()
        val username = fragmentView.findViewById<EditText>(R.id.username)
        val password = fragmentView.findViewById<EditText>(R.id.password)
        val useSSL = fragmentView.findViewById<CheckBox>(R.id.useSSL)
        val login = fragmentView.findViewById<Button>(R.id.login)
        val loading = fragmentView.findViewById<ProgressBar>(R.id.loading)
        val registerButton = fragmentView.findViewById(R.id.register) as Button

        preferences = Preferences(fragmentActivity.applicationContext)
        sharedPrefs = preferences.getDefaultPreferences()

        controller = ClientConfig(args.getString("controller_hostname")!!, 0, "", null)

        Log.d("LoginActivity.onCreate", "controller: " + controller.toString())

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory()).get(LoginViewModel::class.java)
        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            checkSslFlag()
            loginViewModel.login(
                CropDroidAPI(controller, sharedPrefs),
                username.text.toString(),
                password.text.toString()
            )
        }

        try {
            val selectedController = repository.get(controller.hostname)

            Log.d("LoginActivity.onCreate", "selectedController: " + selectedController.hostname)

            if(selectedController != null && !selectedController.token.isEmpty()) {
                (activity as MainActivity).login(selectedController)
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

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }

            if(loginResult.registered) {
                loginViewModel.login(
                    CropDroidAPI(controller, sharedPrefs),
                    username.text.toString(),
                    password.text.toString())
            }

            if(loginResult.success != null) {

                var user = loginResult.success

                Log.d("LoginActivity token:", user.token)

                val jwt = JsonWebToken(requireContext(), user.token)
                Log.d("jwt", jwt.claims.toString())

                var organizations = jwt.organizations()

                Log.d("uid", jwt.uid().toString())
                Log.d("email", jwt.email())
                Log.d("organizations", organizations.toString())
                Log.d("exp", jwt.exp().toString())
                Log.d("iat", jwt.iat().toString())
                Log.d("iss", jwt.iss())

                if(organizations.size == 1 && organizations[0].id == 0L) {

                    val farmId = organizations[0].farms[0].id

                    user.id = jwt.uid().toString()
                    user.orgId = "0"

                    controller.secure = if (useSSL.isChecked) 1 else 0
                    controller.token = user.token
                    controller.jwt = jwt

                    var rowsUpdated = repository.updateController(controller)
                    if (rowsUpdated != 1) {
                        Log.e("LoginActivity.loginResult.token", "Unexpected number of rows effected: " + rowsUpdated.toString())
                        return@Observer
                    }
                    Log.i("LoginActivity.loginResult.token", "Successfully authenticated")

                    preferences.set(controller, user, 0, farmId)

                    (activity as MainActivity).login(controller)
                }
            }

            registerButton.setOnClickListener(View.OnClickListener {
                onRegister(it)
            })
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
                            CropDroidAPI(controller, sharedPrefs),
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }

        return fragmentView
    }

    fun onRegister(v: View) {
        checkSslFlag()
        loginViewModel.register(
            CropDroidAPI(controller, sharedPrefs),
            username.text.toString(),
            password.text.toString()
        )
    }

    fun checkSslFlag() {
        if(useSSL.isChecked()) {
            controller.secure = 1
        } else {
            controller.secure = 0
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(requireActivity().applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(activity as MainActivity, errorString, Toast.LENGTH_SHORT).show()
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