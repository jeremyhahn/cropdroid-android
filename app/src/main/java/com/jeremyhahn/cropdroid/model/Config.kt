package com.jeremyhahn.cropdroid.model

data class Config(val name: String, val interval: String,val timezone: String,  val mode: String, val smtp: SmtpConfig,
    val controllers: ArrayList<Controller> /*, val room: RoomConfig, val reservoir: ReservoirConfig, val doser: DoserConfig*/)