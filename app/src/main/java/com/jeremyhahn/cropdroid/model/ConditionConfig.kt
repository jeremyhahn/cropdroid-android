package com.jeremyhahn.cropdroid.model

data class ConditionConfig(var id: Long, val metricId: Long, var channelId: Long, val comparator: String, val threshold: Double) {

    constructor(id: Long) : this( 0, 0, 0, "", 0.0) {
        this.id = id
    }
}