package com.jeremyhahn.cropdroid.model

data class RoomConfig(val enable: String, val notify: String, val uri: String,
   val video: String, val metrics: ArrayList<Metric>, val channels: ArrayList<Channel>)