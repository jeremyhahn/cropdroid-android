package com.jeremyhahn.cropdroid.model

data class Condition(val id: Long, val controllerType: String, val metricId: Long, val metricName: String, val channelId: Int, val comparator: String, val threshold: Double, val text: String) {

    constructor() : this(0, "", 0, "", 0,"",0.0, "")
}
