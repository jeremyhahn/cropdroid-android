package com.jeremyhahn.cropdroid.model

data class ControllerState(val type: String, val metrics: HashMap<String, Double>, val channels: ArrayList<Int>, val timestamp: String) {
    constructor() : this("",  HashMap(), ArrayList(), "")
}
