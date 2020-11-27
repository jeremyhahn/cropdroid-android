package com.jeremyhahn.cropdroid.model

data class ConditionConfig(var id: String, val metricId: Long, var channelId: Long, val comparator: String, val threshold: Double) {

    constructor(id: String) : this("0", 0, 0, "", 0.0) {
        this.id = id
    }
}