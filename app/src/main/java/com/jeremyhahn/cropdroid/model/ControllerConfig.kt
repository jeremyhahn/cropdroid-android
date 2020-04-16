package com.jeremyhahn.cropdroid.model

data class ControllerConfig(val id: Int, val type: String, val description: String, val enabled: Boolean,
        val notify: Boolean, val uri: String, val hardwareVersion: String, val firmwareVersion: String,
        val metrics: ArrayList<Metric>, val channels: ArrayList<Channel>)