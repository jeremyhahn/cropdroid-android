package com.jeremyhahn.cropdroid.model

data class MasterController(var id: Int, var name: String, var hostname: String, var secure: Int, var userid: Int, var token: String) {
    override fun toString() : String {
        return "MasterController[id=$id name=$name hostname=$hostname secure=$secure userid=$userid, token=$token]"
    }
}
