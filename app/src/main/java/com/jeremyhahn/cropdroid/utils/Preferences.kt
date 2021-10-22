package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_HOSTNAME
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_JWT
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_USER_ID
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.User

class Preferences(context: Context) {

    private val TAG = "Preferences"
    private val context: Context = context

    fun getControllerPreferences() : SharedPreferences {
        //val hostname = getDefaultPreferences().getString(PREF_KEY_CONTROLLER_HOSTNAME, "")
        //return getPreferences(hostname)
        return getDefaultPreferences()
    }

    fun currentController() : String {
        return getDefaultPreferences().getString(PREF_KEY_CONTROLLER_HOSTNAME, "")!!
    }

    fun currentServerId() : Long {
        return getDefaultPreferences().getLong("controller_server", 0L)
    }

    fun currentOrgId() : Long {
        return getDefaultPreferences().getLong(CONFIG_ORG_ID_KEY, 0)
    }

    fun currentFarmId() : Long {
        return getDefaultPreferences().getLong(CONFIG_FARM_ID_KEY, 0)
    }

    fun getDefaultPreferences() : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun set(controller: Connection, user: User?, orgId: Long, farmId: Long) {
        val prefs = getDefaultPreferences()
        val editor = prefs.edit()
        editor.putString(PREF_KEY_CONTROLLER_HOSTNAME, controller.hostname)
        if(user != null) {
            editor.putString(PREF_KEY_USER_ID, user.id)
            editor.putString(PREF_KEY_JWT, user.token)
        }
        editor.putLong(CONFIG_ORG_ID_KEY, orgId)
        editor.putLong(CONFIG_FARM_ID_KEY, farmId)
        if(!editor.commit()) {
            val message = "AppError committing controller to DefaultSharedPreferences"
            Log.e(TAG, message)
            throw RuntimeException(message)
        }
    }

    fun clear() {
        val prefs = getDefaultPreferences()
        val editor = prefs.edit()
        editor.remove(PREF_KEY_CONTROLLER_HOSTNAME)
        editor.remove(PREF_KEY_USER_ID)
        editor.remove(PREF_KEY_JWT)
        if(!editor.commit()) {
            val message = "AppError clearing controller and user keys from DefaultSharedPreferences"
            Log.e(TAG, message)
            throw RuntimeException(message)
        }
    }
}
