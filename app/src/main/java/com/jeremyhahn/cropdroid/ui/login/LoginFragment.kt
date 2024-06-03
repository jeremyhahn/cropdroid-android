package com.jeremyhahn.cropdroid.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.cast.CastStatusCodes.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.Constants.Companion.APP_SERVER_CLIENT_ID
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_HOSTNAME
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_PUBKEY
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import com.jeremyhahn.cropdroid.utils.Preferences
import io.jsonwebtoken.ExpiredJwtException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class LoginFragment() : Fragment(), View.OnClickListener {

    private lateinit var repository: EdgeDeviceRepository
    private lateinit var preferences: Preferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var connection: Connection
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gsoButton: SignInButton
    private lateinit var loading: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val fragmentActivity = requireActivity()
        repository = EdgeDeviceRepository(fragmentActivity.applicationContext)

        var fragmentView = inflater.inflate(R.layout.fragment_login, container, false)
        val mainActivity = (activity as MainActivity)
        val args = requireArguments()
        val username = fragmentView.findViewById<EditText>(R.id.username)
        val password = fragmentView.findViewById<EditText>(R.id.password)
        val useSSL = fragmentView.findViewById<CheckBox>(R.id.useSSL)
        val login = fragmentView.findViewById<Button>(R.id.login)
        loading = fragmentView.findViewById<ProgressBar>(R.id.loading)
        val registerButton = fragmentView.findViewById(R.id.register) as Button
        gsoButton = fragmentView.findViewById(R.id.gso_sign_in_button) as SignInButton
        gsoButton.setOnClickListener(this)

        val gsoOptions = GoogleSignInOptions.Builder().
            requestId().
            requestProfile().
            requestServerAuthCode(APP_SERVER_CLIENT_ID).
            requestIdToken(APP_SERVER_CLIENT_ID).
            build()
        val gso = GoogleSignInOptions.Builder(gsoOptions).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(fragmentActivity, gso)

        preferences = Preferences(fragmentActivity.applicationContext)
        sharedPrefs = preferences.getDefaultPreferences()
        connection = Connection(args.getString(PREF_KEY_CONTROLLER_HOSTNAME)!!,
            0,
            "",
            args.getString(PREF_KEY_CONTROLLER_PUBKEY)!!,
            null)

        Log.d("LoginActivity.onCreate", "controller: " + connection.toString())

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory()).get(LoginViewModel::class.java)
        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            checkSslFlag()

            val organizationNameEditText = fragmentView.findViewById(R.id.organizationName) as EditText
            loginViewModel.login(
                CropDroidAPI(connection, sharedPrefs),
                organizationNameEditText.text.toString(),
                username.text.toString(),
                password.text.toString()
            )
        }

        try {

            val selectedController = repository.get(connection.hostname)

            Log.d("LoginActivity.onCreate", "selectedController: " + selectedController)

            if(selectedController != null && !selectedController.token.isEmpty() ) {
                Log.d("LoginActivity.onCreate", "token not empty, sending to mainActivity.login(selectedController)...")

                mainActivity.connection = selectedController
                val cropDroidAPI = CropDroidAPI(selectedController, sharedPrefs)

                if(selectedController.jwt!!.claims == null) {
                    mainActivity.logout()
                    return fragmentView
                }

                cropDroidAPI.refreshToken(selectedController.jwt!!.uid(), object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.d("LoginViewModel.login", "onFailure response: " + e!!.message)
                        showLoginFailed(e.message.toString())
                        mainActivity.logout()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        Log.d("refreshToken", "refreshToken response: " + response)
                        var responseBody = response.body().string()
                        Log.d("refreshToken", "responseBody: " + responseBody)

                        val jwt = JsonWebToken(requireContext(), selectedController)
                        jwt.parse()
                        Log.d("jwt", jwt.claims.toString())

                        val organizations = jwt.organizations()
                        val farms = jwt.farms()

                        Log.d("uid", jwt.uid().toString())
                        Log.d("email", jwt.email())
                        Log.d("organizations", organizations.toString())
                        Log.d("farms", farms.toString())
                        Log.d("exp", jwt.exp().toString())
                        Log.d("iat", jwt.iat().toString())
                        Log.d("iss", jwt.iss())

                        selectedController.token = jwt.claims.toString()
                        repository.updateController(selectedController)

                        fragmentActivity.runOnUiThread {
                            mainActivity.navigateToFarms(selectedController)
                        }
                    }
                })
                //mainActivity.navigateToFarms(selectedController)
                return fragmentView
            }
        }
        catch(e: ExpiredJwtException) {
            Log.d("LoginActivity.onCreate", "JWT expired")
            connection.token = ""
            return fragmentView
        }

        for(controller in repository.allControllers) {
            Log.d("LoginActivity.onCreate", "registered controller: " + controller!!.toString())
        }

        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer {
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

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            val loginResult = it ?: return@Observer
            //val cropDroidAPI = CropDroidAPI(connection, sharedPrefs)

            loading.visibility = View.GONE
            //setResult(Activity.RESULT_OK)

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }

            if(loginResult.registered) {
                showSuccessfulRegistration()
//                loginViewModel.login(
//                    cropDroidAPI,
//                    organizationName.text.toString(),
//                    username.text.toString(),
//                    password.text.toString())
            }

            if(loginResult.user != null) {

                var user = loginResult.user

                Log.d("LoginActivity token:", user.token)
                connection.token = user.token

                try {
                    val jwt = JsonWebToken(requireContext(), connection)
                    jwt.parse()
                    Log.d("jwt", jwt.claims.toString())

                    var organizations = jwt.organizations()
                    var farms = jwt.farms()

                    Log.d("uid", jwt.uid().toString())
                    Log.d("email", jwt.email())
                    Log.d("organizations", organizations.toString())
                    Log.d("farms", farms.toString())
                    Log.d("exp", jwt.exp().toString())
                    Log.d("iat", jwt.iat().toString())
                    Log.d("iss", jwt.iss())

                    user.id = jwt.uid().toString()
                    user.username = jwt.email()

                    connection.secure = if (useSSL.isChecked) 1 else 0
                    connection.token = user.token
                    connection.jwt = jwt

                    var rowsUpdated = repository.updateController(connection)
                    if (rowsUpdated != 1) {
                        Log.e(
                            "LoginActivity.loginResult.token",
                            "Unexpected number of rows effected: " + rowsUpdated.toString()
                        )
                        return@Observer
                    }
                    Log.i("LoginActivity.loginResult.token", "Successfully authenticated")

//                    val serviceIntent = Intent(context, NotificationService::class.java)
//                    mainActivity.startService(serviceIntent)

                    // TODO: Refactor to support users belonging to multiple organizations
                    //                if(organizations.isNotEmpty()) {
                    //                    for (organization in organizations) {
                    //                        val orgId = organization.id
                    //                        if (organizations[0].farms.size > 0) {
                    //                            farmId = organizations[0].farms[0].id
                    //                        }
                    //                        user.id = jwt.uid().toString()
                    //                        user.orgId = orgId.toString()
                    //
                    //                        mainActivity.navigateToOrganizations(connection, user)
                    //                        return
                    //                    }
                    //                }

                    //mainActivity.login(connection)
                    mainActivity.setLoggedInUser(user)
                    mainActivity.navigateToFarms(connection, user, 0)
                }
                catch(e: Exception) {
                    Log.d("LoginFragment", "JWT Parser exception, line 269")
                    Log.d("LoginFragmet", e.message!!)
                    e.message?.let { it1 -> AppError(requireActivity()).alert(it1, null, null) }
                }
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

            val orgNameEditText = fragmentView.findViewById(R.id.organizationName) as EditText
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            CropDroidAPI(connection, sharedPrefs),
                            orgNameEditText.text.toString(),
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }

        registerButton.setOnClickListener(View.OnClickListener {
            onRegister(it)
        })

        val account = GoogleSignIn.getLastSignedInAccount(fragmentActivity)
        if(account != null && !connection.token.isEmpty()) {
            Log.d("LoginFragment.onCreate", "Google session already established, bypassing login...")

            //(activity as MainActivity).login(connection)
            (activity as MainActivity).navigateToFarms(connection)
        }

        return fragmentView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.gso_sign_in_button -> {
                loading.visibility = VISIBLE
                signIn()
            }
        }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        loading.visibility = GONE

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {

        val cropdroid = CropDroidAPI(connection, sharedPrefs)

        try {

            val account = completedTask.getResult(ApiException::class.java)
            Log.w("LoginFragment", "account: " + account)
            Log.w("LoginFragment", "account.idToken " + account!!.idToken)
            Log.w("LoginFragment", "account.serverAuthCode " + account.serverAuthCode)
            loginViewModel.googleLogin(cropdroid, account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes.html

            val retryStatusCodes = HashMap<Int, Boolean>()
            retryStatusCodes[NETWORK_ERROR] = true
            retryStatusCodes[INTERRUPTED] = true
            retryStatusCodes[CANCELED] = true
            retryStatusCodes[TIMEOUT] = true

            if(retryStatusCodes.containsKey(e.statusCode)) {
                Thread.sleep(500)
                handleSignInResult(completedTask)
            }

            Log.w("LoginFragment", "signInResult:failed code=" + e.statusCode)
        }
    }


    fun onRegister(v: View) {
        checkSslFlag()

        val orgNameEditText = v.findViewById(R.id.organizationName) as EditText
        val usernameTextEdit = v.findViewById(R.id.username) as EditText
        val passwordTextEdit = v.findViewById(R.id.password) as EditText

        val email = usernameTextEdit.text.toString()
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            AppError(requireActivity()).alert("Invalid email address", null, null)
            return
        }
        loginViewModel.register(
            CropDroidAPI(connection, sharedPrefs),
            orgNameEditText.text.toString(),
            email,
            passwordTextEdit.text.toString()
        )
    }

    fun checkSslFlag() {
        val useSSLCheckBox = requireActivity().findViewById(R.id.useSSL) as CheckBox
        if(useSSLCheckBox.isChecked()) {
            connection.secure = 1
        } else {
            connection.secure = 0
        }
    }

//    private fun showLoginFailed(@StringRes errorString: Int) {
//        Toast.makeText(requireActivity().applicationContext, errorString, Toast.LENGTH_SHORT).show()
//    }

    private fun showLoginFailed(errorString: String) {
        val mainActivity = (activity as MainActivity)
        requireActivity().runOnUiThread({
            Toast.makeText(mainActivity, errorString, Toast.LENGTH_SHORT).show()
        })
    }

    private fun showSuccessfulRegistration() {
        requireActivity().runOnUiThread({
            val mainActivity = (activity as MainActivity)
            Toast.makeText(mainActivity, "Registration successful, check your inbox for an activation email!", Toast.LENGTH_SHORT).show()
        })
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