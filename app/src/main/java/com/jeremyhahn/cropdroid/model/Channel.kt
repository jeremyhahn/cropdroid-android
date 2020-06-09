package com.jeremyhahn.cropdroid.model

data class Channel(var id: Long, var controllerId: Long, var channelId: Long, var name: String, var enable: Boolean, var notify: Boolean,
                   var duration: Int, var debounce: Int, var backoff: Int, var algorithmId: Int, var value: Int) {

    fun isEnabled() : Boolean {
        return enable
    }
}