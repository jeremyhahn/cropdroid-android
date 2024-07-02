package com.jeremyhahn.cropdroid.model

data class ControllerStateDelta(val type: String, val metrics: HashMap<String, Double>, val channels: HashMap<Int, Int>, val timestamp: String) {
    constructor() : this("",  HashMap(), HashMap(), "")
}
