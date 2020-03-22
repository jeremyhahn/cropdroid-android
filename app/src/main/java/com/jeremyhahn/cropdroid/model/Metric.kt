package com.jeremyhahn.cropdroid.model

data class Metric(var id: Int, var key: String, var name: String, var enable: Boolean, var notify: Boolean,
   var unit: String, var alarmLow: Double, var alarmHigh: Double, var value: Double)