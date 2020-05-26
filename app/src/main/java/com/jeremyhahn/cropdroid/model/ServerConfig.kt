package com.jeremyhahn.cropdroid.model

data class ServerConfig(val name: String, val interval: String, val timezone: String, val mode: String,
                        val smtp: SmtpConfig, val organizations: ArrayList<Organization>, val farms: ArrayList<Farm>) {

    constructor() : this("", "", "", "", SmtpConfig(), ArrayList<Organization>(), ArrayList<Farm>())
}