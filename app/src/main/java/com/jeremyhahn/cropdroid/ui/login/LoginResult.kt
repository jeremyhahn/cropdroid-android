package com.jeremyhahn.cropdroid.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null,
    val message: String? = null,
    val registered: Boolean = false,
    val token: String? = null
)
