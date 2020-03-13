package com.jeremyhahn.cropdroid.model

data class Channel(val id: Int, val name: String, val enable: Boolean, val state: Int) {

    fun isEnabled() : Boolean {
        return enable
    }

    fun switchStateAsString() : String {
        if(state === 1) {
            return "ON"
        }
        else {
            return "OFF"
        }
    }
}