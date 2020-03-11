package com.jeremyhahn.cropdroid.model

data class ReservoirConfig(val enable: String, val notify: String, val uri: String, val gallons: String,
   val targetTemp: String, val waterChange: WaterChangeConfig, val metrics: ArrayList<Metric>, val channels: ArrayList<Channel>)