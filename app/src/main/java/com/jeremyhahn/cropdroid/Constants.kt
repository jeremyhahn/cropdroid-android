package com.jeremyhahn.cropdroid

class Constants {

    companion object {

        const val APP_NAME = "cropdroid"
        const val DATABASE_NAME = APP_NAME
        const val GLOBAL_PREFS = APP_NAME
        const val PREF_KEY_CONTROLLER_ID = "controller_id"
        const val API_VERSION = "v1"
        const val API_BASE = "/api/".plus(API_VERSION)
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_QUIT = "ACTION_QUIT"
        const val NOTIFICATION_PRIORITY_LOW = 0
        const val NOTIFICATION_PRIORITY_MED = 1
        const val NOTIFICATION_PRIORITY_HIGH = 2

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