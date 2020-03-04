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
        const val WATER_LEAK_STATUS_DRY = "Dry"
        const val WATER_LEAK_STATUS_LEAK = "Leaking!"
        const val FLOAT_SWITCH_FULL = "Floating"
        const val FLOAT_SWITCH_LOW = "Water Low!"
        const val ROOM_CONTROLLER_ID = 0
        const val RESERVOIR_CONTROLLER_ID = 1
        const val DOSING_CONTROLLER_ID = 2
        const val MICROCONTROLLER_REFRESH = 30000L // 30 seconds

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