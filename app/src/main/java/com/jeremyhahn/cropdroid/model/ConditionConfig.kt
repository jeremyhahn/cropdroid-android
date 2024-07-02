package com.jeremyhahn.cropdroid.model

data class ConditionConfig(var id: Long, val metricId: Long, val workflowId: Long,
                           var channelId: Long, val comparator: String, val threshold: Double) {

    constructor(id: Long) : this(0L, 0, 0, 0, "", 0.0) {
        this.id = id
    }
}