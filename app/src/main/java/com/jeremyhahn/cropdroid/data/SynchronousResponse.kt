package com.jeremyhahn.cropdroid.data

import okhttp3.Response

data class SynchronousResponse(
    val response: Response?,
    val error: Exception?
)

