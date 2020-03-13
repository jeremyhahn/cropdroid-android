package com.jeremyhahn.cropdroid.model

data class Metric(val id: String, val name: String, val enable: String, val notify: String,
   val display: String, val unit: String, val alarmLow: String, val alarmHigh: String, val value: String)