package com.jeremyhahn.cropdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        startActivity(Intent(this, MicroControllerActivity::class.java))
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var controller : MasterController? = null
        private var cropdroid: CropDroidAPI? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).registerOnSharedPreferenceChangeListener(this);

            val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
                .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)

            controller = MasterControllerRepository(context!!).getController(id)
            cropdroid = CropDroidAPI(controller!!)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            Log.d("SettingsActivity.onSharedPreferenceChanged", "key: " + key)
            var value = ""
            try {
                value = sharedPreferences!!.getString(key, "")
            }
            catch(e: ClassCastException) {
                if (e.message.equals("java.lang.Boolean cannot be cast to java.lang.String")) {
                    value = sharedPreferences!!.getBoolean(key, false).toString()
                }
            }
            cropdroid!!.setConfig(controller!!.serverId.toString(), key!!, value, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("SettingsActivity.onPreferenceClick()", "onFailure response: " + e!!.message)
                }
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    var responseBody = response.body().string()
                    Log.d("SettingsActivity.onResponse", "responseBody: " + responseBody)
                }
            })
        }

        fun createValue(sharedPreferences: SharedPreferences, key: String) : String {
            return sharedPreferences!!.getString(key, "")
        }

        fun createValue(sharedPreferences: SharedPreferences, key: Boolean) : String {
            return sharedPreferences!!.getString(key.toString(), "")
        }

    }
}