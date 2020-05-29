package com.jeremyhahn.cropdroid.model

data class Farm(val id: Long, val orgId: Long, val mode: String, val name: String, val interval: Int,
                val timezone: String, val smtp: SmtpConfig, val controllers: ArrayList<Controller>, var roles: List<String>) {
    constructor(id: Long, orgId: Long, name: String) : this(id, orgId, "", name, 0, "", SmtpConfig(), ArrayList(), ArrayList())
}