package com.jeremyhahn.cropdroid.model

data class Farm(val id: Long, val orgId: Int, val mode: String, val name: String, val interval: Int, val controllers: ArrayList<Controller>, val roles: List<String>)