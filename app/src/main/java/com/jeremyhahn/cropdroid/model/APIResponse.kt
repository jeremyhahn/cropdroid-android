package com.jeremyhahn.cropdroid.model

data class APIResponse(val code: Int, val error: String, val success: Boolean, var payload: Any?)
