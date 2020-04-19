package com.jeremyhahn.cropdroid.utils

import android.content.SharedPreferences
import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FIRMWARE_VERSION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_HARDWARE_VERSION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NOTIFY_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_HOST_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PASSWORD_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PORT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_RECIPIENT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_USERNAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_URI_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_VIDEO_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.model.Config
import com.jeremyhahn.cropdroid.model.Controller

class ConfigManager(val sharedPreferences: SharedPreferences, val config: Config) {

    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun sync() {
        Log.d("ConfigManager.sync", "Running sync. controller_id=" + getInt(PREF_KEY_CONTROLLER_ID))
        syncGlobal()
        syncSmtp()
        syncControllers()
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

    private fun syncControllers() {

        editor.putInt("controller_count", config.controllers.size)

        for(controller in config.controllers) {

            editor.putInt("controller_" + controller.type, controller.id)

            val enable = getBoolean(controller.type + "." + CONFIG_ENABLE_KEY)
            val notify = getBoolean(controller.type + "." + CONFIG_NOTIFY_KEY)
            val uri = getString(controller.type + "." + CONFIG_URI_KEY)
            val video = getString(controller.type + "." + CONFIG_VIDEO_KEY)
            val hardwareVersion = getString(controller.type + "." + CONFIG_HARDWARE_VERSION_KEY)
            val firmwareVersion = getString(controller.type + "." + CONFIG_FIRMWARE_VERSION_KEY)
            if(enable != controller.enabled) {
                setEditorValue(controller.type + "." + CONFIG_ENABLE_KEY, controller.enabled)
            }
            if(notify != controller.notify) {
                setEditorValue(controller.type + "." + CONFIG_NOTIFY_KEY, controller.notify)
            }
            if(uri != controller.uri) {
                setEditorValue(controller.type + "." + CONFIG_URI_KEY, controller.uri)
            }
            if(hardwareVersion != controller.hardwareVersion) {
                setEditorValue(controller.type + "." + CONFIG_HARDWARE_VERSION_KEY, controller.hardwareVersion)
            }
            if(firmwareVersion != controller.firmwareVersion) {
                setEditorValue(controller.type + "." + CONFIG_FIRMWARE_VERSION_KEY, controller.firmwareVersion)
            }
        }
    }

    private fun setEditorValue(key: String, value: String) {
        editor.putString(key, value)
    }

    private fun setEditorValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
