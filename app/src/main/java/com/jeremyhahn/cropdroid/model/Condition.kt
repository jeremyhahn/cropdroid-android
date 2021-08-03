package com.jeremyhahn.cropdroid.model

data class Condition(val id: String, val deviceType: String, val metricId: Long,
                     val metricName: String, val workflowId: Long, val channelId: Int,
                     val comparator: String, val threshold: Double, val text: String) {

    constructor() : this("0", "", 0, "", 0,0,"",0.0, "")
}
