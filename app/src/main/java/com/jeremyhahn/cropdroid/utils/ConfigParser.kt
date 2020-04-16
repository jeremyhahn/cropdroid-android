package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CONTROLLERS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.model.Config
import com.jeremyhahn.cropdroid.model.ControllerConfig
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
                config.getString(CONFIG_NAME_KEY),
                config.getString(CONFIG_INTERVAL_KEY),
                config.getString(CONFIG_TIMEZONE_KEY),
                config.getString(CONFIG_MODE_KEY),
                parseSmtp(config.getJSONObject(CONFIG_SMTP_KEY)),
                parseControllers(config.getJSONArray(CONFIG_CONTROLLERS_KEY))
            )
            /*
                parseRoom(config.getJSONObject(CONFIG_ROOM_KEY)),
                parseReservoir(config.getJSONObject(CONFIG_RESERVOIR_KEY)),
                parseDoser(config.getJSONObject(CONFIG_DOSER_KEY)))
                */
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
        fun parseRoom(room : JSONObject) : RoomConfig {
            Log.d("parseRoom", room.toString())
            return RoomConfig(
                room.getString("enable"),
                room.getString("notify"),
                room.getString("uri"),
                room.getString("video"),
                MetricParser.parse(room.getJSONArray("metrics")),
                ChannelParser.parse(room.getJSONArray("channels")))
        }

        fun parseReservoir(room : JSONObject) : ReservoirConfig {
            Log.d("ConfigParser.parseReservoir", room.toString())
            return ReservoirConfig(
                room.getString("enable"),
                room.getString("notify"),
                room.getString("uri"),
                room.getString("gallons"),
                room.getString("targetTemp"),
                parseWaterChangeConfig(room.getJSONObject("waterchange")),
                MetricParser.parse(room.getJSONArray("metrics")),
                ChannelParser.parse(room.getJSONArray("channels")))
        }

        fun parseDoser(doser : JSONObject) : DoserConfig {
            Log.d("parseDoser", doser.toString())
            return DoserConfig(
                doser.getString("enable"),
                doser.getString("notify"),
                doser.getString("uri"),
                ChannelParser.parse(doser.getJSONArray("channels")))
        }

        fun parseWaterChangeConfig(waterChange: JSONObject) : WaterChangeConfig {
            return WaterChangeConfig(
                waterChange.getBoolean("enable"),
                waterChange.getBoolean("notify"),
                waterChange.getString("subscribes"))
        }
*/

        fun parseControllers(jsonControllers: JSONArray): ArrayList<ControllerConfig> {
            Log.d("parseControllers", jsonControllers.toString())

            var controllers = ArrayList<ControllerConfig>(jsonControllers.length())
            for (i in 0..jsonControllers.length() - 1) {

                val jsonChannel = jsonControllers.getJSONObject(i)

                Log.d("ConfigParser.parseControllers", jsonChannel.toString())

                val id = jsonChannel.getInt("id")
                val type = jsonChannel.getString("type")
                val description = jsonChannel.getString("description")
                val enabled = jsonChannel.getBoolean("enable")
                val notify = jsonChannel.getBoolean("notify")
                val uri = jsonChannel.getString("uri")
                val hardwareVersion = jsonChannel.getString("hardwareVersion")
                val firmwareVersion = jsonChannel.getString("firmwareVersion")
                val metrics = MetricParser.parse(jsonChannel.getJSONArray("metrics"))
                val channels = ChannelParser.parse(jsonChannel.getJSONArray("channels"))
                controllers.add(ControllerConfig(id, type, description, enabled, notify, uri, hardwareVersion, firmwareVersion, metrics, channels))
            }
            return controllers
        }
    }
}