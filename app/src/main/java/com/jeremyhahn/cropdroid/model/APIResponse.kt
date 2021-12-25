package com.jeremyhahn.cropdroid.model

data class APIResponse(val code: Int, val error: String, val success: Boolean, val payload: Any)
