package com.jeremyhahn.cropdroid.model

data class Condition(val id: Int, val controllerType: String, val metricId: Int, val metricName: String, val channelId: Int, val comparator: String, val threshold: Double, val text: String) {

    constructor() : this(0, "", 0, "", 0,"",0.0, "")
}
