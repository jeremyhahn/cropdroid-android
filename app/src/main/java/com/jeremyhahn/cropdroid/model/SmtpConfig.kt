package com.jeremyhahn.cropdroid.model

data class SmtpConfig(val enable: String, val host: String, val port: String, val username: String, val password: String, val to: String)