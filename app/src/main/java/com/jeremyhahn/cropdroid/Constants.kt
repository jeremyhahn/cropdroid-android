package com.jeremyhahn.cropdroid

class Constants {

    companion object {

        const val API_VERSION = "v1"
        const val DATABASE_NAME = "cropdroid"
        const val GLOBAL_PREFS = "cropdroid"
        const val PREF_KEY_USER_ID = "user_id"
        const val PREF_KEY_CONTROLLER_ID = "controller_id"
        const val PREF_KEY_CONTROLLER_HOSTNAME = "controller_hostname"
        const val PREF_KEY_CONTROLLER_SECURE = "controller_secure"
        const val PREF_KEY_JWT = "jwt"
        const val API_BASE = "/api/".plus(API_VERSION)
        const val API_CONNECTION_UNAVAILABLE_RETRY_COUNT = 100000000
        const val API_CONNECTION_UNAVAILABLE_RETRY_BACKOFF = 1.0f

        enum class SwitchState {
            ON,
            OFF
        }

        enum class ControllerType {
            Room,
            Reservoir,
            Doser
        }
    }
}