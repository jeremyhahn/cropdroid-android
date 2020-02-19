package com.jeremyhahn.cropdroid.model

data class MasterController(var id: Int, var name: String, var hostname: String, var token: String) {
    override fun toString() : String {
        return "MasterController[id=$id name=$name hostname=$hostname token=$token]"
    }
}
