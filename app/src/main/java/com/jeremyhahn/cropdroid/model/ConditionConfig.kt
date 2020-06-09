package com.jeremyhahn.cropdroid.model

data class ConditionConfig(var id: Int, val metricId: Long, var channelId: Long, val comparator: String, val threshold: Double) {

    constructor(id: Int) : this( 0, 0, 0, "", 0.0) {
        this.id = id
    }
}