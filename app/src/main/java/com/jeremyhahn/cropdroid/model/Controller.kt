package com.jeremyhahn.cropdroid.model

data class Controller(var id: Int, var orgId: Int, var type: String,
     var description: String, var hardwareVersion: String, var softwareVersion: String,
     var metrics: List<Metric>, var channels: List<Channel>)