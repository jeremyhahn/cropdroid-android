package com.jeremyhahn.cropdroid

import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker.RecurrenceOption

class Constants {

    companion object {

        const val APP_NAME = "cropdroid"
        const val DATABASE_NAME = APP_NAME
        //const val DATE_FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ssz"
          const val DATE_FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ssXXX"

        const val CONFIG_CONTROLLER_COUNT_KEY = "controller_count"
        const val CONFIG_CONTROLLER_PREFIX_KEY = "controller_"

        const val PREF_KEY_CONTROLLER_ID = "controller_id"
        const val PREF_KEY_CONTROLLER_HOSTNAME = "controller_hostname"
        const val PREF_KEY_USER_ID = "user_id"
        const val PREF_KEY_JWT = "jwt"
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

        const val CONFIG_ID_KEY = "id"
        const val CONFIG_INTERVAL_KEY = "interval"
        const val CONFIG_TIMEZONE_KEY = "timezone"
        const val CONFIG_MODE_KEY = "mode"
        const val CONFIG_LICENSE_KEY = "license"

        const val CONFIG_ORG_ID_KEY = "org.id"
        const val CONFIG_ORG_NAME_KEY = "org.name"

        const val CONFIG_FARM_ID_KEY = "id"
        const val CONFIG_FARM_ORG_ID_KEY = "orgId"
        const val CONFIG_FARM_NAME_KEY = "name"
        const val CONFIG_FARM_MODE_KEY = "mode"
        const val CONFIG_FARM_INTERVAL_KEY = "interval"

        const val CONFIG_MODE_VIRTUAL = "virtual"
        const val CONFIG_MODE_SERVER = "server"
        const val CONFIG_MODE_CLOUD = "cloud"
        const val CONFIG_MODE_MAINTENANCE = "maintenance"

        const val CONFIG_SMTP_KEY = "smtp"
        const val CONFIG_SMTP_ENABLE_KEY = "smtp.enable"
        const val CONFIG_SMTP_HOST_KEY = "smtp.host"
        const val CONFIG_SMTP_PORT_KEY = "smtp.port"
        const val CONFIG_SMTP_USERNAME_KEY = "smtp.username"
        const val CONFIG_SMTP_PASSWORD_KEY = "smtp.password"
        const val CONFIG_SMTP_RECIPIENT_KEY = "smtp.recipient"

        const val CONFIG_ORGS_KEY = "organizations"
        const val CONFIG_FARMS_KEY = "farms"
        const val CONFIG_ROLES_KEY = "roles"
        const val CONFIG_CONTROLLERS_KEY = "controllers"
        const val CONFIG_TYPE_KEY = "type"
        const val CONFIG_DESCRIPTION_KEY = "description"
        //const val CONFIG_ENABLE_KEY = "enable"
        //const val CONFIG_NOTIFY_KEY = "notify"
        //const val CONFIG_URI_KEY = "uri"
        //const val CONFIG_VIDEO_KEY = "video"
        const val CONFIG_HARDWARE_VERSION_KEY = "hardwareVersion"
        const val CONFIG_FIRMWARE_VERSION_KEY = "firmwareVersion"

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
        const val CONFIG_CHANNEL_CONTROLLER_ID_KEY = "controllerId"
        const val CONFIG_CHANNEL_NAME_KEY = "name"
        const val CONFIG_CHANNEL_ENABLE_KEY = "enable"
        const val CONFIG_CHANNEL_NOTIFY_KEY = "notify"
        const val CONFIG_CHANNEL_CONDITION_KEY = "condition"
        const val CONFIG_CHANNEL_SCHEDULE_KEY = "schedule"
        const val CONFIG_CHANNEL_DURATION_KEY = "duration"
        const val CONFIG_CHANNEL_DEBOUNCE_KEY = "debounce"
        const val CONFIG_CHANNEL_BACKOFF_KEY = "backoff"
        const val CONFIG_CHANNEL_ALGORITHM_ID_KEY = "algorithmId"

        enum class SwitchState {
            ON,
            OFF
        }

        enum class ControllerType {
            Room,
            Reservoir,
            Doser
        }

        const val SCHEDULE_TIME_ONLY_FORMAT = "h:mm:ss a"
        //const val SCHEDULE_DATE_DISPLAY_FORMAT = "MM-dd-yyyy hh:mm a"
        const val SCHEDULE_DATE_TIME_LONG_FORMAT = "EEE, MMM d, yyyy '@' h:mm a z"
        const val SCHEDULE_NULL_DATE = "0001-01-01T00:00:00Z"

        const val SCHEDULE_TYPE_ONCE = 0
        const val SCHEDULE_TYPE_DAILY = 1
        const val SCHEDULE_TYPE_WEEKLY = 2
        const val SCHEDULE_TYPE_MONTHLY = 3
        const val SCHEDULE_TYPE_YEARLY = 4
        const val SCHEDULE_TYPE_CUSTOM = 5

        val SCHEDULE_FREQUENCY_MAP: HashMap<Int, RecurrenceOption> = hashMapOf(
            0 to RecurrenceOption.DOES_NOT_REPEAT,
            1 to RecurrenceOption.DAILY,
            2 to RecurrenceOption.WEEKLY,
            3 to RecurrenceOption.MONTHLY,
            4 to RecurrenceOption.YEARLY,
            5 to RecurrenceOption.CUSTOM)

        val SCHEDULE_FREQUENCY_ID_MAP: HashMap<String, Int> = hashMapOf(
            RecurrenceOption.DOES_NOT_REPEAT.name to SCHEDULE_TYPE_ONCE,
            RecurrenceOption.DAILY.name to SCHEDULE_TYPE_DAILY,
            RecurrenceOption.WEEKLY.name to SCHEDULE_TYPE_WEEKLY,
            RecurrenceOption.MONTHLY.name to SCHEDULE_TYPE_MONTHLY,
            RecurrenceOption.YEARLY.name to SCHEDULE_TYPE_YEARLY,
            RecurrenceOption.CUSTOM.name to SCHEDULE_TYPE_CUSTOM)

        val SCHEDULE_DAY_MAP: HashMap<String, String> = hashMapOf(
            "SU" to "Sunday",
            "MO" to "Monday",
            "TU" to "Tuesday",
            "WE" to "Wednesday",
            "TH" to "Thursday",
            "FR" to "Friday",
            "SA" to "Saturday")

        const val SECONDS_IN_MINUTE = 60
        const val SECONDS_IN_HOUR = 3600
        const val SECONDS_IN_DAY = 86400
        const val SECONDS_IN_WEEK = 604800
        const val SECONDS_IN_MONTH = 2629800
        const val SECONDS_IN_YEAR = 31557600
    }
}