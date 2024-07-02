package com.jeremyhahn.cropdroid.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit private var controller : Connection
    lateinit private var cropdroid: CropDroidAPI
    lateinit var sharedPreferences: SharedPreferences
    lateinit var preferences: Preferences
    private var farmId: Long = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        //super.onCreatePreferences(savedInstanceState)

        val fragmentActivity = requireActivity()

        preferences = Preferences(fragmentActivity)

        val hostname = preferences.currentController()
        farmId = preferences.currentFarmId()

        sharedPreferences = preferences.getControllerPreferences()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        controller = EdgeDeviceRepository(fragmentActivity).get(hostname)!!
        cropdroid = CropDroidAPI(controller, sharedPreferences)

        Log.d("SettingsFragment.onCreatePreferences", "controller=" + controller.toString())

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val controllerId = parseControllerId(key!!)!!
        var value = ""
        try {
            value = sharedPreferences!!.getString(key, "")!!
        }
        catch(e: ClassCastException) {
            if (e.message.equals("java.lang.Boolean cannot be cast to java.lang.String")) {
                value = sharedPreferences!!.getBoolean(key, false).toString()
            }
/*
            if (e.message.equals("java.lang.Integer cannot be cast to java.lang.String")) {
                value = sharedPreferences!!.getInt(key, 0).toString()
            }
 */
        }
        Log.d("SettingsActivity.onSharedPreferenceChanged", "key=" + key + ", value="+ value + ", controller=" + controller.toString())
        cropdroid.setConfig(controllerId, key, value, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("SettingsActivity.onPreferenceClick()", "onFailure response: " + e.message)
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()
                Log.d("SettingsActivity.onResponse", "responseBody: " + responseBody)
            }
        })
    }

    /*
    override fun onSupportNavigateUp(): Boolean {
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
        activity!!.onNavigateUp()

        //startActivity(Intent(this, MicroControllerActivity::class.java))

        return true
    }*/

//    override fun onSupportNavigateUp(): Boolean {
//        val mainActivity = (activity as MainActivity)
//        val fragmentActivity = requireActivity()
//        if (fragmentActivity.supportFragmentManager.popBackStackImmediate()) {
//            return true
//        }
//        return mainActivity.onSupportNavigateUp()
//    }

    private fun parseControllerId(key: String) : String {
        val pieces = key.split(".")
        if(pieces.size == 1) {
            //return farmId.toString()
            return preferences.currentServerId().toString()
        }
        val controller_key = Constants.CONFIG_CONTROLLER_PREFIX_KEY.plus(pieces[0]) // ex: controller_room
        Log.d("SettingsActivity", "Looking for controller key: " + controller_key)
        return sharedPreferences!!.getLong(controller_key, 1).toString()
    }
}