package com.jeremyhahn.cropdroid.ui.login

import com.jeremyhahn.cropdroid.model.User

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: User? = null,
    val error: String? = null,
    val registered: Boolean = false
)
