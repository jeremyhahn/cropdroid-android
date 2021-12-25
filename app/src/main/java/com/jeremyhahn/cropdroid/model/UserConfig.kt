package com.jeremyhahn.cropdroid.model

data class UserConfig(var id: Long, var email: String, var password: String, var roles: ArrayList<RoleConfig>) {
    constructor() : this(0L, "", "", ArrayList())
}