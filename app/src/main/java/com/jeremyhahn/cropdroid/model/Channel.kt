package com.jeremyhahn.cropdroid.model

data class Channel(var id: Int, var controllerId: Int, var channelId: Int, var name: String, var enable: Boolean, var notify: Boolean,
                   var duration: Int, var debounce: Int, var backoff: Int, var algorithmId: Int, var value: Int) {

    fun isEnabled() : Boolean {
        return enable
    }
}