package com.jeremyhahn.cropdroid.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ORG_ID_KEY
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
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.Error
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.lang.Integer.parseInt
import java.lang.Thread.sleep

class ConfigManager(val mainActivity: MainActivity, val sharedPreferences: SharedPreferences) : WebSocketListener() {

    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    var websockets : HashMap<WebSocket, ClientConfig> = HashMap()
    private val TAG = "ConfigManager"

    private var farmId: Long = 0

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val CONNECTION_FAILED_DELAY = 60000L
    }
/*
    fun sync() {
        Log.d(TAG, "Running sync. controller_id=" + getInt(PREF_KEY_CONTROLLER_HOSTNAME))
        for(org in config.organizations) {
            syncOrganization(org)
        }
        syncSmtp()
        editor.commit()
    }
*/
    fun listen(farmId: Long) {
        this.farmId = farmId
        //val websocket = cropDroidAPI.createWebsocket(context, "/farms/$farmId/config/changefeed", this)
        val websocket = mainActivity.cropDroidAPI.createWebsocket(mainActivity, "/changefeed/$farmId", this)
        if(websocket != null) websockets[websocket] = mainActivity.cropDroidAPI.controller
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

    fun getLong(key: String) : Long {
        return sharedPreferences.getLong(key, 0)
    }

    private fun syncOrganization(org: Organization) {
        val id = getLong(CONFIG_ORG_ID_KEY)
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
        Log.d(TAG, "syncFarm: " + farm.toString())

        // sync controllers first to overwrite farm level settings
        // with correct data types (controller settings are always strings)
        for(controller in farm.controllers) {
            syncControllers(farm.controllers)
        }

        val id = getLong(CONFIG_FARM_ID_KEY)
        val orgId = getLong(CONFIG_FARM_ORG_ID_KEY)
        val name = getString(CONFIG_FARM_NAME_KEY)
        val mode = getString(CONFIG_FARM_MODE_KEY)
        val interval = getString(CONFIG_FARM_INTERVAL_KEY)
        val timezone = getString(CONFIG_TIMEZONE_KEY)
        if(id != farm.id) {
            setEditorValue(CONFIG_FARM_ID_KEY, farm.id)
        }
        if(orgId != farm.orgId) {
            setEditorValue(CONFIG_FARM_ORG_ID_KEY, farm.orgId)
        }
        if(name != farm.name) {
            setEditorValue(CONFIG_FARM_NAME_KEY, farm.name)
        }
        if(mode != farm.mode) {
            setEditorValue(CONFIG_FARM_MODE_KEY, farm.mode)
        }
        if(parseInt(interval) != farm.interval) {
            setEditorValue(CONFIG_FARM_INTERVAL_KEY, farm.interval)
            //editor.putInt(CONFIG_FARM_INTERVAL_KEY, farm.interval)
        }
        if(timezone != farm.timezone) {
            setEditorValue(CONFIG_TIMEZONE_KEY, farm.timezone)
        }
        syncSmtp(farm.smtp)
    }

    private fun syncSmtp(smtpConfig: SmtpConfig) {
        val enable = getBoolean(CONFIG_SMTP_ENABLE_KEY)
        val host = getString(CONFIG_SMTP_HOST_KEY)
        val port = getString(CONFIG_SMTP_PORT_KEY)
        val username = getString(CONFIG_SMTP_USERNAME_KEY)
        val password = getString(CONFIG_SMTP_PASSWORD_KEY)
        val to = getString(CONFIG_SMTP_RECIPIENT_KEY)

        val bEnable = smtpConfig.enable.toBoolean()

        Log.d("syncSmtp", "bEnable=" + bEnable + ", enable="+ enable)

        if(enable != bEnable) {
            setEditorValue(CONFIG_SMTP_ENABLE_KEY, bEnable)
        }
        if(host != smtpConfig.host) {
            setEditorValue(CONFIG_SMTP_HOST_KEY, smtpConfig.host)
        }
        if(port != smtpConfig.port) {
            setEditorValue(CONFIG_SMTP_PORT_KEY, smtpConfig.port)
        }
        if(username != smtpConfig.username) {
            setEditorValue(CONFIG_SMTP_USERNAME_KEY, smtpConfig.username)
        }
        if(password != smtpConfig.password) {
            setEditorValue(CONFIG_SMTP_PASSWORD_KEY, smtpConfig.password)
        }
        if(to != smtpConfig.to) {
            setEditorValue(CONFIG_SMTP_RECIPIENT_KEY, smtpConfig.to)
        }
    }

    private fun syncControllers(controllers: List<Controller>) {

        // val countKey = farmID.toString().plus("_controller_count")
        editor.putInt("controller_count", controllers.size)

        for(controller in controllers) {

            // val typeKey := farmID.toString().plus("_controller_" + controller.type)
            editor.putLong("controller_" + controller.type, controller.id)

            for ((k, v) in controller.configs) {

                /*
                if(controller.type.equals("server") && k.equals("interval")) {
                    editor.putInt(k, v)
                    continue
                }*/

                when(v) {
                    is Boolean -> editor.putBoolean(k, v)
                    //is Integer -> editor.putInt(k, v.toInt())
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

    private fun setEditorValue(key: String, value: Long) {
        editor.putLong(key, value)
    }
    override fun onOpen(webSocket: WebSocket, response: Response) {
        val controller =  websockets[webSocket]
        if(controller != null) {
            var payload = "{\"id\":" + controller.jwt!!.uid().toString() + "}"
            Log.d("ConfigManager.WebSocket.onOpen", "controller="  + controller.hostname + ", payload=" + payload)
            webSocket.send(payload)
            return
        }
        //webSocket.send(ByteString.decodeHex("test"))
        //webSocket.close(Companion.NORMAL_CLOSURE_STATUS, "Peace out!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("ConfigManager.onMessage(text)", text)
        try {
            val farm = FarmParser.parse(JSONObject(text), 0L, false)
            syncFarm(farm)
            editor.commit()
            mainActivity.update(farm)
        }
        catch(e: Exception) {
            Error(mainActivity).alert(e.message.toString(), null, null)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("ConfigManager.onMessage(bytes)", bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("ConfigManager.onClosing", "$code / $reason")
        var controller = websockets[webSocket]
        if(controller != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            return
        }
        Log.d("ConfigManager.onFailure", "Unable to locate controller for closed websocket connection: " + webSocket.hashCode())
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {

        t.printStackTrace()

        Log.d("ConfigManager.onFailure", "response: " + response.toString())
        Log.d("ConfigManager.onFailure", "throwable: " + t.toString())

        sleep(CONNECTION_FAILED_DELAY)

        var controller = websockets[webSocket]
        if(controller != null) {
            Log.d("COnfigManager.onFailure", "Restarting connection for " + controller.hostname)
            webSocket.cancel()
            if(farmId > 0) listen(farmId)
            return
        }

        Log.d("ConfigManager.onFailure", "Unable to locate controller for failed websocket connection: " + webSocket.hashCode())
    }
/*
    override fun register(o: ConfigObserver) {
        this.observers.add(o)
    }

    override fun unregister(o: ConfigObserver) {
        this.observers.remove(o)
    }

    override fun updateObservers(controller: Controller) {
        for(observer in observers) {
            observer.updateConfig(controller)
        }
    }
 */
}
