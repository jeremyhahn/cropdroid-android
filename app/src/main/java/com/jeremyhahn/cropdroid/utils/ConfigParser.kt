package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CONTROLLERS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARMS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.model.Config
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.model.SmtpConfig
import org.json.JSONArray
import org.json.JSONObject

class ConfigParser {

    companion object {
        fun parse(json: String): Config {
            return parse(JSONObject(json))
        }

        fun parse(config: JSONObject): Config {
            return Config(
                //config.getString(CONFIG_NAME_KEY),
                "",
                config.getString(CONFIG_INTERVAL_KEY),
                config.getString(CONFIG_TIMEZONE_KEY),
                config.getString(CONFIG_MODE_KEY),
                parseSmtp(config.getJSONObject(CONFIG_SMTP_KEY)),
                parseFarms(config.getJSONArray(CONFIG_FARMS_KEY))
            )
        }

        fun parseSmtp(smtp: JSONObject): SmtpConfig {
            Log.d("parseSmtp", smtp.toString())
            return SmtpConfig(
                smtp.getString("enable"),
                smtp.getString("host"),
                smtp.getString("port"),
                smtp.getString("username"),
                smtp.getString("password"),
                smtp.getString("recipient")
            )
        }
/*
        fun parseWaterChangeConfig(waterChange: JSONObject) : WaterChangeConfig {
            return WaterChangeConfig(
                waterChange.getBoolean("enable"),
                waterChange.getBoolean("notify"),
                waterChange.getString("subscribes"))
        }
*/

        fun parseFarms(jsonFarms: JSONArray): ArrayList<Farm> {

            Log.d("parseFarms", jsonFarms.toString())

            var farms = ArrayList<Farm>(jsonFarms.length())
            for (i in 0..jsonFarms.length() - 1) {

                val jsonFarm = jsonFarms.getJSONObject(i)

                Log.d("ConfigParser.parseFarms", jsonFarm.toString())

                val id = jsonFarm.getInt("id")
                val orgId = jsonFarm.getInt("orgId")
                val mode = jsonFarm.getString("mode")
                val name = jsonFarm.getString("name")
                val interval = jsonFarm.getInt("interval")
                val controllers = parseControllers(jsonFarm.getJSONArray(CONFIG_CONTROLLERS_KEY))
                farms.add(Farm(id, orgId, mode, name, interval, controllers))
            }
            return farms
        }

        fun parseControllers(jsonControllers: JSONArray): ArrayList<Controller> {
            Log.d("parseControllers", jsonControllers.toString())

            var controllers = ArrayList<Controller>(jsonControllers.length())
            for (i in 0..jsonControllers.length() - 1) {

                val jsonChannel = jsonControllers.getJSONObject(i)

                Log.d("ConfigParser.parseControllers", jsonChannel.toString())

                val jsonConfigs = jsonChannel.getJSONObject("configs")
                val configs = HashMap<String, String>(jsonConfigs.length())
                for (i in 0 until jsonConfigs.length()) {
                    val k = jsonConfigs.keys().next()
                    val v = jsonConfigs.getString(k)
                    configs.put(k, v)
                    Log.i("ConfigParser.parseControllers", "Putting config -- Key: " + k + ", value: " + v)
                }
                val id = jsonChannel.getInt("id")
                val orgId = jsonChannel.getInt("orgId")
                val type = jsonChannel.getString("type")
                val description = jsonChannel.getString("description")
                //val enabled = jsonChannel.getBoolean("enable")
                //val notify = jsonChannel.getBoolean("notify")
                //val uri = jsonChannel.getString("uri")
                val hardwareVersion = jsonChannel.getString("hardwareVersion")
                val firmwareVersion = jsonChannel.getString("firmwareVersion")
                val metrics = MetricParser.parse(jsonChannel.getJSONArray("metrics"))
                val channels = ChannelParser.parse(jsonChannel.getJSONArray("channels"))
                //controllers.add(Controller(id, orgId, type, description, enabled, notify, uri, hardwareVersion, firmwareVersion, metrics, channels))
                controllers.add(Controller(id, orgId, type, description, hardwareVersion, firmwareVersion, configs, metrics, channels))
            }
            return controllers
        }
    }
}