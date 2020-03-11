package com.jeremyhahn.cropdroid.utils

import android.content.SharedPreferences
import android.util.Log
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
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_TO_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_USERNAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.model.Config
import com.jeremyhahn.cropdroid.model.User

class ConfigManager(val sharedPreferences: SharedPreferences, val config: Config) {

    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun sync() {
        Log.d("ConfigManager.sync", "Running sync!")
        syncGlobal()
        syncSmtp()
        syncRoom()
        syncReservoir()
        syncDoser()
        editor.commit()
    }

    fun getValue(key: String) : String {
        return sharedPreferences.getString(key, "")
    }

    fun getBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    private fun syncGlobal() {
        val name = getValue(CONFIG_NAME_KEY)
        val interval = getValue(CONFIG_INTERVAL_KEY)
        val timezone = getValue(CONFIG_TIMEZONE_KEY)
        val mode = getValue(CONFIG_MODE_KEY)
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
        val host = getValue(CONFIG_SMTP_HOST_KEY)
        val port = getValue(CONFIG_SMTP_PORT_KEY)
        val username = getValue(CONFIG_SMTP_USERNAME_KEY)
        val password = getValue(CONFIG_SMTP_PASSWORD_KEY)
        val to = getValue(CONFIG_SMTP_TO_KEY)
        val bEnable = config.room.enable.toBoolean()

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
            setEditorValue(CONFIG_SMTP_TO_KEY, config.smtp.to)
        }
    }

    private fun syncRoom() {
        val enable = getBoolean(CONFIG_ROOM_ENABLE_KEY)
        val notify = getBoolean(CONFIG_ROOM_NOTIFY_KEY)
        val uri = getValue(CONFIG_ROOM_URI_KEY)
        val video = getValue(CONFIG_ROOM_VIDEO_KEY)
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
        val uri = getValue(CONFIG_RESERVOIR_URI_KEY)
        val gallons = getValue(CONFIG_RESERVOIR_GALLONS_KEY)
        val targetTemp = getValue(CONFIG_RESERVOIR_TARGET_TEMP_KEY)
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
        val uri = getValue(CONFIG_DOSER_URI_KEY)
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
/*
    private fun syncChannels(controllerType: String, channels: ArrayList<Channel>) {

        for(channel in channels) {
            val id = getValue(CONFIG_CHANNEL_ID_KEY)
            val key = getValue(CONFIG_CHANNEL_NAME_KEY)
            val enable = getValue(CONFIG_CHANNEL_ENABLE_KEY)
            val notify = getValue(CONFIG_CHANNEL_NOTIFY_KEY)
            val condition = getValue(CONFIG_CHANNEL_CONDITION_KEY)
            val schedule = getValue(CONFIG_CHANNEL_SCHEDULE_KEY)
            val duration = getValue(CONFIG_CHANNEL_DURATION_KEY)
            val debounce = getValue(CONFIG_CHANNEL_DEBOUNCE_KEY)
            val backoff = getValue(CONFIG_CHANNEL_BACKOFF_KEY)
            if(id != config.doser.enable) {
                setEditorValue(CONFIG_DOSER_ENABLE_KEY, enable)
            }
            if(notify != config.doser.notify) {
                setEditorValue(CONFIG_DOSER_NOTIFY_KEY, notify)
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
