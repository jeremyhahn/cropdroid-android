package com.jeremyhahn.cropdroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class SettingsActivity : AppCompatActivity() {

    lateinit private var settingsFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.settings, settingsFragment).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.beginTransaction().remove(settingsFragment).commit()
        finish()
        startActivity(Intent(this, MicroControllerActivity::class.java))
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        lateinit private var controller : MasterController
        lateinit private var cropdroid: CropDroidAPI
        lateinit var sharedPreferences: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            //super.onCreatePreferences(savedInstanceState)

            val preferences = Preferences(activity!!.applicationContext)
            val id = preferences.currentControllerId()

            sharedPreferences = preferences.getControllerPreferences()
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)

            controller = MasterControllerRepository(context!!).getController(id)
            cropdroid = CropDroidAPI(controller!!)

            Log.d("SettingsActivity.onCreate", "controller=" + controller.toString())

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

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            var value = ""
            try {
                value = sharedPreferences!!.getString(key, "")
            }
            catch(e: ClassCastException) {
                if (e.message.equals("java.lang.Boolean cannot be cast to java.lang.String")) {
                    value = sharedPreferences!!.getBoolean(key, false).toString()
                }
            }
            Log.d("SettingsActivity.onSharedPreferenceChanged", "key=" + key + ", value="+ value + ", controller=" + controller!!.toString())
            cropdroid!!.setConfig(key!!, value, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("SettingsActivity.onPreferenceClick()", "onFailure response: " + e!!.message)
                }
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    var responseBody = response.body().string()
                    Log.d("SettingsActivity.onResponse", "responseBody: " + responseBody)
                }
            })
        }
    }
}