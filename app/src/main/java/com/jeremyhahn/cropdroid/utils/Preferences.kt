package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_HOSTNAME
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_NAME
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_SERVER_ID
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_JWT
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_USER_ID
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.User

class Preferences(context: Context) {

    private val TAG = "Preferences"
    private val context: Context = context

    fun getControllerPreferences() : SharedPreferences {
        //val hostname = getDefaultPreferences().getString(PREF_KEY_CONTROLLER_HOSTNAME, "")
        //return getPreferences(hostname)
        return getDefaultPreferences()
    }

    fun currentControllerId() : Int {
        return getDefaultPreferences().getInt(PREF_KEY_CONTROLLER_ID, 0)
    }

    fun currentOrgId() : Int {
        return getDefaultPreferences().getInt(CONFIG_ORG_ID_KEY, 0)
    }

    fun currentFarmId() : Int {
        return getDefaultPreferences().getInt(CONFIG_FARM_ID_KEY, 0)
    }

    fun getDefaultPreferences() : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun set(controller: MasterController, user: User?, orgId: Int, farmId: Int) {
        val prefs = getDefaultPreferences()
        val editor = prefs.edit()
        editor.putInt(PREF_KEY_CONTROLLER_ID, controller.id)
        editor.putInt(PREF_KEY_CONTROLLER_SERVER_ID, controller.serverId)
        editor.putString(PREF_KEY_CONTROLLER_NAME, controller.name)
        editor.putString(PREF_KEY_CONTROLLER_HOSTNAME, controller.hostname)
        if(user != null) {
            editor.putString(PREF_KEY_USER_ID, user.id)
            editor.putString(PREF_KEY_JWT, user.token)
        }
        editor.putInt(CONFIG_ORG_ID_KEY, orgId)
        editor.putInt(CONFIG_FARM_ID_KEY, farmId)
        if(!editor.commit()) {
            val message = "Error committing controller to DefaultSharedPreferences"
            Log.e(TAG, message)
            throw RuntimeException(message)
        }
    }

    fun clear() {
        val prefs = getDefaultPreferences()
        val editor = prefs.edit()
        editor.remove(PREF_KEY_CONTROLLER_ID)
        editor.remove(PREF_KEY_CONTROLLER_SERVER_ID)
        editor.remove(PREF_KEY_CONTROLLER_NAME)
        editor.remove(PREF_KEY_CONTROLLER_HOSTNAME)
        editor.remove(PREF_KEY_USER_ID)
        editor.remove(PREF_KEY_JWT)
        if(!editor.commit()) {
            val message = "Error clearing controller and user keys from DefaultSharedPreferences"
            Log.e(TAG, message)
            throw RuntimeException(message)
        }
    }
}
