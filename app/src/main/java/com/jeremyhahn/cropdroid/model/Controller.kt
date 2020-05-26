package com.jeremyhahn.cropdroid.model

data class Controller(val id: Int, val type: String, val description: String,
                      /*val enabled: Boolean, val notify: Boolean, val uri: String,*/
                      val hardwareVersion: String, val firmwareVersion: String,
                      val configs: HashMap<String, Any>, val metrics: List<Metric>, val channels: List<Channel>)
