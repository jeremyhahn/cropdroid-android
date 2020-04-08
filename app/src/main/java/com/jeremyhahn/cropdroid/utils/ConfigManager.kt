package com.jeremyhahn.cropdroid.utils

import android.content.SharedPreferences
import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_ALGORITHM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_BACKOFF_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_CONDITION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_CONTROLLER_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_DEBOUNCE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_DURATION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_NOTIFY_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CHANNEL_SCHEDULE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_DOSER_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_DOSER_NOTIFY_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_DOSER_URI_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_GALLONS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_NOTIFY_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_TARGET_TEMP_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_URI_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_NOTIFY_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_URI_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_VIDEO_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_HOST_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PASSWORD_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PORT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_RECIPIENT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_USERNAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Config

class ConfigManager(val sharedPreferences: SharedPreferences, val config: Config) {

    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun sync() {
        Log.d("ConfigManager.sync", "Running sync. controller_id=" + getInt(PREF_KEY_CONTROLLER_ID))
        syncGlobal()
        syncSmtp()
        syncRoom()
        syncReservoir()
        syncDoser()
        editor.commit()
    }

    fun getString(key: String) : String {
        return sharedPreferences.getString(key, "")
    }

    fun getBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun getInt(key: String) : Int {
        return sharedPreferences.getInt(key, 0)
    }

    private fun syncGlobal() {
        val name = getString(CONFIG_NAME_KEY)
        val interval = getString(CONFIG_INTERVAL_KEY)
        val timezone = getString(CONFIG_TIMEZONE_KEY)
        val mode = getString(CONFIG_MODE_KEY)
        if(name != config.name) {
            setEditorValue(CONFIG_NAME_KEY, config.name)
        }
        if(interval != config.interval) {
            setEditorValue(CONFIG_INTERVAL_KEY, config.interval)
        }
        if(timezone != config.timezone) {
            setEditorValue(CONFIG_TIMEZONE_KEY, config.timezone)
        }
        if(mode != config.mode) {
            setEditorValue(CONFIG_MODE_KEY, config.mode)
        }
    }

    private fun syncSmtp() {
        val enable = getBoolean(CONFIG_SMTP_ENABLE_KEY)
        val host = getString(CONFIG_SMTP_HOST_KEY)
        val port = getString(CONFIG_SMTP_PORT_KEY)
        val username = getString(CONFIG_SMTP_USERNAME_KEY)
        val password = getString(CONFIG_SMTP_PASSWORD_KEY)
        val to = getString(CONFIG_SMTP_RECIPIENT_KEY)

        val bEnable = config.smtp.enable.toBoolean()

        Log.d("syncSmtp", "bEnable=" + bEnable + ", enable="+ enable)

        if(enable != bEnable) {
            setEditorValue(CONFIG_SMTP_ENABLE_KEY, bEnable)
        }
        if(host != config.smtp.host) {
            setEditorValue(CONFIG_SMTP_HOST_KEY, config.smtp.host)
        }
        if(port != config.smtp.port) {
            setEditorValue(CONFIG_SMTP_PORT_KEY, config.smtp.port)
        }
        if(username != config.smtp.username) {
            setEditorValue(CONFIG_SMTP_USERNAME_KEY, config.smtp.username)
        }
        if(password != config.smtp.password) {
            setEditorValue(CONFIG_SMTP_PASSWORD_KEY, config.smtp.password)
        }
        if(to != config.smtp.to) {
            setEditorValue(CONFIG_SMTP_RECIPIENT_KEY, config.smtp.to)
        }
    }

    private fun syncRoom() {
        val enable = getBoolean(CONFIG_ROOM_ENABLE_KEY)
        val notify = getBoolean(CONFIG_ROOM_NOTIFY_KEY)
        val uri = getString(CONFIG_ROOM_URI_KEY)
        val video = getString(CONFIG_ROOM_VIDEO_KEY)
        val bEnable = config.room.enable.toBoolean()
        val bNotify = config.room.notify.toBoolean()
        if(enable != bEnable) {
            setEditorValue(CONFIG_ROOM_ENABLE_KEY, bEnable)
        }
        if(notify != bNotify) {
            setEditorValue(CONFIG_ROOM_NOTIFY_KEY, bNotify)
        }
        if(uri != config.room.uri) {
            setEditorValue(CONFIG_ROOM_URI_KEY, config.room.uri)
        }
        if(video != config.room.video) {
            setEditorValue(CONFIG_ROOM_VIDEO_KEY, config.room.video)
        }
    }

    private fun syncReservoir() {
        val enable = getBoolean(CONFIG_RESERVOIR_ENABLE_KEY)
        val notify = getBoolean(CONFIG_RESERVOIR_NOTIFY_KEY)
        val uri = getString(CONFIG_RESERVOIR_URI_KEY)
        val gallons = getString(CONFIG_RESERVOIR_GALLONS_KEY)
        val targetTemp = getString(CONFIG_RESERVOIR_TARGET_TEMP_KEY)
        val bEnable = config.reservoir.enable.toBoolean()
        val bNotify = config.reservoir.notify.toBoolean()
        if(enable != bEnable) {
            setEditorValue(CONFIG_RESERVOIR_ENABLE_KEY, bEnable)
        }
        if(notify != bNotify) {
            setEditorValue(CONFIG_RESERVOIR_NOTIFY_KEY, bNotify)
        }
        if(uri != config.reservoir.uri) {
            setEditorValue(CONFIG_RESERVOIR_URI_KEY, config.reservoir.uri)
        }
        if(gallons != config.reservoir.gallons) {
            setEditorValue(CONFIG_RESERVOIR_GALLONS_KEY, config.reservoir.gallons)
        }
        if(targetTemp != config.reservoir.targetTemp) {
            setEditorValue(CONFIG_RESERVOIR_TARGET_TEMP_KEY, config.reservoir.targetTemp)
        }
    }

    private fun syncDoser() {
        val enable = getBoolean(CONFIG_DOSER_ENABLE_KEY)
        val notify = getBoolean(CONFIG_DOSER_NOTIFY_KEY)
        val uri = getString(CONFIG_DOSER_URI_KEY)
        val bEnable = config.doser.enable.toBoolean()
        val bNotify = config.doser.notify.toBoolean()
        if(enable != bEnable) {
            setEditorValue(CONFIG_DOSER_ENABLE_KEY, bEnable)
        }
        if(notify != bNotify) {
            setEditorValue(CONFIG_DOSER_NOTIFY_KEY, bNotify)
        }
        if(uri != config.doser.uri) {
            setEditorValue(CONFIG_DOSER_URI_KEY, config.doser.uri)
        }
    }

    private fun channelKey(controllerType: String, id: Int, key: String) : String {
        return controllerType + ".channel." + id + "." + key
    }
/*
    private fun syncChannels(controllerType: String, channels: ArrayList<Channel>) {

        for((i, channel) in channels.withIndex()) {
            val id = getString(channelKey(controllerType, i, CONFIG_CHANNEL_ID_KEY))
            val controllerId = getString(channelKey(controllerType, i, CONFIG_CHANNEL_CONTROLLER_ID_KEY))
            val key = getString(channelKey(controllerType, i, CONFIG_CHANNEL_NAME_KEY))
            val enable = getBoolean(channelKey(controllerType, i, CONFIG_CHANNEL_ENABLE_KEY))
            val notify = getBoolean(channelKey(controllerType, i, CONFIG_CHANNEL_NOTIFY_KEY))
            val condition = getString(channelKey(controllerType, i, CONFIG_CHANNEL_CONDITION_KEY))
            val schedule = getString(channelKey(controllerType, i, CONFIG_CHANNEL_SCHEDULE_KEY))
            val duration = getString(channelKey(controllerType, i, CONFIG_CHANNEL_DURATION_KEY))
            val debounce = getString(channelKey(controllerType, i, CONFIG_CHANNEL_DEBOUNCE_KEY))
            val backoff = getString(channelKey(controllerType, i, CONFIG_CHANNEL_BACKOFF_KEY))
            val algorithmId = getString(channelKey(controllerType, i, CONFIG_CHANNEL_ALGORITHM_ID_KEY))

            val bEnable = enable.toBoolean()
            val bNotify = config.doser.notify.toBoolean()
            if(channel.enable != bEnable) {
                setEditorValue(channelKey(controllerType, i, CONFIG_DOSER_ENABLE_KEY), bEnable)
            }
            if(channel.notify != bNotify) {
                setEditorValue(channelKey(controllerType, i, CONFIG_DOSER_NOTIFY_KEY), bNotify)
            }
            if(id != channel.id) {
                setEditorValue(channelKey(controllerType, i, CONFIG_CHANNEL_ID_KEY), channel.id)
            }
        }
    }
*/

    private fun setEditorValue(key: String, value: String) {
        editor.putString(key, value)
    }

    private fun setEditorValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
