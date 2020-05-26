package com.jeremyhahn.cropdroid.model

data class MasterController(var id: Int, var serverId: Int, var name: String, var hostname: String, var secure: Int, var userid: Long, var token: String) {
    override fun toString() : String {
        return "MasterController[id=$id serverId=$serverId name=$name hostname=$hostname secure=$secure userid=$userid, token=$token]"
    }
}
