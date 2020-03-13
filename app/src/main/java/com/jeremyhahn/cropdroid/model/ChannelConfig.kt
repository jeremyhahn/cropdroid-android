package com.jeremyhahn.cropdroid.model

data class ChannelConfig(val id: Int, val name: String, val enable: String, val notify: String,
                   val condition: String, val schedule: String, val duration: String,
                   val debounce: String, val backoff: String, val state: Int)