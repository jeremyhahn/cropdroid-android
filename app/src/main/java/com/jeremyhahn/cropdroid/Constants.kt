package com.jeremyhahn.cropdroid

class Constants {

    companion object {

        const val APP_NAME = "cropdroid"
        const val DATABASE_NAME = APP_NAME
        //const val GLOBAL_PREFS = APP_NAME
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

        const val CONFIG_NAME_KEY = "name"
        const val CONFIG_INTERVAL_KEY = "interval"
        const val CONFIG_TIMEZONE_KEY = "timezone"
        const val CONFIG_MODE_KEY = "mode"

        const val CONFIG_SMTP_KEY = "smtp"
        const val CONFIG_SMTP_ENABLE_KEY = "smtp.enable"
        const val CONFIG_SMTP_HOST_KEY = "smtp.host"
        const val CONFIG_SMTP_PORT_KEY = "smtp.port"
        const val CONFIG_SMTP_USERNAME_KEY = "smtp.username"
        const val CONFIG_SMTP_PASSWORD_KEY = "smtp.password"
        const val CONFIG_SMTP_TO_KEY = "smtp.to"

        const val CONFIG_ROOM_KEY = "room"
        const val CONFIG_ROOM_ENABLE_KEY = "room.enable"
        const val CONFIG_ROOM_NOTIFY_KEY = "room.notify"
        const val CONFIG_ROOM_URI_KEY = "room.uri"
        const val CONFIG_ROOM_VIDEO_KEY = "room.video"

        const val CONFIG_RESERVOIR_KEY = "reservoir"
        const val CONFIG_RESERVOIR_ENABLE_KEY = "reservoir.enable"
        const val CONFIG_RESERVOIR_NOTIFY_KEY = "reserovir.notify"
        const val CONFIG_RESERVOIR_URI_KEY = "reservoir.uri"
        const val CONFIG_RESERVOIR_GALLONS_KEY = "reservoir.gallons"
        const val CONFIG_RESERVOIR_TARGET_TEMP_KEY = "reservoir.targetTemp"

        const val CONFIG_DOSER_KEY = "doser"
        const val CONFIG_DOSER_ENABLE_KEY = "doser.enable"
        const val CONFIG_DOSER_NOTIFY_KEY = "doser.notify"
        const val CONFIG_DOSER_URI_KEY = "doser.uri"

        const val CONFIG_CHANNEL_ID_KEY = "id"
        const val CONFIG_CHANNEL_NAME_KEY = "name"
        const val CONFIG_CHANNEL_ENABLE_KEY = "enable"
        const val CONFIG_CHANNEL_NOTIFY_KEY = "notify"
        const val CONFIG_CHANNEL_CONDITION_KEY = "condition"
        const val CONFIG_CHANNEL_SCHEDULE_KEY = "schedule"
        const val CONFIG_CHANNEL_DURATION_KEY = "duration"
        const val CONFIG_CHANNEL_DEBOUNCE_KEY = "debounce"
        const val CONFIG_CHANNEL_BACKOFF_KEY = "backoff"

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