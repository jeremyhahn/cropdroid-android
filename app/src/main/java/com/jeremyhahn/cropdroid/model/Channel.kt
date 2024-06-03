package com.jeremyhahn.cropdroid.model

data class Channel(var id: Long, var controllerId: Long, var boardId: Long, var name: String, var enable: Boolean, var notify: Boolean,
                   var duration: Int, var debounce: Int, var backoff: Int, var algorithmId: Long, var value: Int) {

    fun isEnabled() : Boolean {
        return enable
    }
}