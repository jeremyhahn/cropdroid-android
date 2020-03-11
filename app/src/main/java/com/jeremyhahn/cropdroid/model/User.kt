package com.jeremyhahn.cropdroid.model

data class User(var id: String, var username: String, var password: String,
    var token: String, var orgId: String, var role: String)