package com.jeremyhahn.cropdroid.model

data class Channel(val id: Int, val name: String, val state: Int) {

    fun switchStateAsString() : String {
        if(state === 1) {
            return "ON"
        }
        else {
            return "OFF"
        }
    }
}