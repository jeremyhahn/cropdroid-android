package com.jeremyhahn.cropdroid.model

data class ConditionConfig(var id: Int, val metricId: Int, var channelId: Int, val comparator: String, val threshold: Double) {

    constructor(id: Int) : this( 0, 0, 0, "", 0.0) {
        this.id = id
    }
}