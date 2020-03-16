package com.jeremyhahn.cropdroid.model

data class Channel(var id: Int, var channelId: Int, var name: String, var enable: Boolean, var notify: Boolean,
                   var condition: String, var schedule: String, var duration: Int,
                   var debounce: Int, var backoff: Int, var value: Int) {

    fun isEnabled() : Boolean {
        return enable
    }

    fun switchStateAsString() : String {
        if(value === 1) {
            return "ON"
        }
        else {
            return "OFF"
        }
    }
}