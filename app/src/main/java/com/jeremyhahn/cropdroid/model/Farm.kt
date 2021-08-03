package com.jeremyhahn.cropdroid.model
/*
data class Farm(val id: Long, val orgId: Long, val replicas: Int, val consistency: Int, val stateStore: Int,
                val configStore: Int, val mode: String, val name: String, val interval: Int, val timezone: String,
                val publicKey: String, val smtp: SmtpConfig, val controllers: ArrayList<Controller>,
                val users: List<Workflow>, val workflows: List<Workflow>) {
*/
data class Farm(val id: Long, val orgId: Long, val mode: String, val name: String, val interval: Int,
                val timezone: String, val smtp: SmtpConfig, val controllers: ArrayList<Controller>,
                val roles: List<String>, var workflows: ArrayList<Workflow>) {

    constructor(id: Long, orgId: Long, name: String) : this(id, orgId, "", name, 0, "",
         SmtpConfig(), ArrayList(), ArrayList(), ArrayList())
}