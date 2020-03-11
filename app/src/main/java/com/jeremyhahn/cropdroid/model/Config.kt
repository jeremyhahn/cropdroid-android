package com.jeremyhahn.cropdroid.model

import android.content.Context
import com.jeremyhahn.cropdroid.Constants

data class Config(val name: String, val interval: String,val timezone: String,  val mode: String, val smtp: SmtpConfig,
    val room: RoomConfig, val reservoir: ReservoirConfig, val doser: DoserConfig)