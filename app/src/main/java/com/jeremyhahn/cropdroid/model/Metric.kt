package com.jeremyhahn.cropdroid.model

data class Metric(val id: String, val name: String, val display: String, val unit: String, val enabled: String,
        val notify: String, val alarmLow: String, val alarmHigh: String, val value: String)