package com.jeremyhahn.cropdroid.model

data class Condition(val id: Int, val channelId: Int, val metricId: Int, val comparator: String, val threshold: Double)