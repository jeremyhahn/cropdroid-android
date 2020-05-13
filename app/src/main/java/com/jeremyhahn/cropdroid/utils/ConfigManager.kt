package com.jeremyhahn.cropdroid.utils

import android.content.SharedPreferences
import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FIRMWARE_VERSION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_HARDWARE_VERSION_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_ENABLE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_HOST_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PASSWORD_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_PORT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_RECIPIENT_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_USERNAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.model.Organization
import com.jeremyhahn.cropdroid.model.ServerConfig

class ConfigManager(val sharedPreferences: SharedPreferences, val config: ServerConfig) {

    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun sync() {
        Log.d("ConfigManager.sync", "Running sync. controller_id=" + getInt(PREF_KEY_CONTROLLER_ID))
        for(org in config.organizations) {
            syncOrganization(org)
        }
        syncSmtp()
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

    private fun syncOrganization(org: Organization) {
        val id = getInt(CONFIG_ORG_ID_KEY)
        val name = getString(CONFIG_ORG_NAME_KEY)
        if(id != org.id) {
            setEditorValue(CONFIG_ORG_ID_KEY, org.id)
        }
        if(name != org.name) {
            setEditorValue(CONFIG_ORG_NAME_KEY, org.name)
        }
        for(farm in org.farms) {
            syncFarm(farm)
        }
    }

    private fun syncFarm(farm: Farm) {
        val id = getInt(CONFIG_FARM_ID_KEY)
        val name = getString(CONFIG_FARM_NAME_KEY)
        val mode = getString(CONFIG_FARM_MODE_KEY)
        val interval = getInt(CONFIG_FARM_INTERVAL_KEY)
        //val timezone = getString(CONFIG_TIMEZONE_KEY)
        if(id != farm.id) {
            setEditorValue(CONFIG_FARM_ID_KEY, farm.id)
        }
        if(name != farm.name) {
            setEditorValue(CONFIG_FARM_NAME_KEY, farm.name)
        }
        if(mode != farm.mode) {
            setEditorValue(CONFIG_FARM_MODE_KEY, farm.mode)
        }
        if(interval != farm.interval) {
            setEditorValue(CONFIG_FARM_INTERVAL_KEY, farm.interval)
        }
        /*
        if(timezone != config.timezone) {
            setEditorValue(CONFIG_TIMEZONE_KEY, config.timezone)
        }*/
        for(controller in farm.controllers) {
            syncControllers(farm.controllers)
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

    private fun syncControllers(controllers: List<Controller>) {

        // val countKey = farmID.toString().plus("_controller_count")
        editor.putInt("controller_count", controllers.size)

        for(controller in controllers) {

            // val typeKey := farmID.toString().plus("_controller_" + controller.type)
            editor.putInt("controller_" + controller.type, controller.id)

            for ((k, v) in controller.configs) {
                when(v) {
                    is Boolean -> editor.putBoolean(k, v)
                    else -> editor.putString(k, v.toString())
                }
            }

            val hardwareVersion = getString(controller.type + "." + CONFIG_HARDWARE_VERSION_KEY)
            if(hardwareVersion != controller.hardwareVersion) {
                setEditorValue(controller.type + "." + CONFIG_HARDWARE_VERSION_KEY, controller.hardwareVersion)
            }
            val firmwareVersion = getString(controller.type + "." + CONFIG_FIRMWARE_VERSION_KEY)
            if(firmwareVersion != controller.firmwareVersion) {
                setEditorValue(controller.type + "." + CONFIG_FIRMWARE_VERSION_KEY, controller.firmwareVersion)
            }
        }
    }

    private fun setEditorValue(key: String, value: Int) {
        editor.putInt(key, value)
    }

    private fun setEditorValue(key: String, value: String) {
        editor.putString(key, value)
    }

    private fun setEditorValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
