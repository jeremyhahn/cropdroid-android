package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_DOSER_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_RESERVOIR_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.model.*
import org.json.JSONObject

class ConfigParser {

    companion object {
        fun parse(json: String): Config {
            return parse(JSONObject(json))
        }

        fun parse(config : JSONObject) : Config {
            return Config(
                config.getString(CONFIG_NAME_KEY),
                config.getString(CONFIG_INTERVAL_KEY),
                config.getString(CONFIG_TIMEZONE_KEY),
                config.getString(CONFIG_MODE_KEY),
                parseSmtp(config.getJSONObject(CONFIG_SMTP_KEY)),
                parseRoom(config.getJSONObject(CONFIG_ROOM_KEY)),
                parseReservoir(config.getJSONObject(CONFIG_RESERVOIR_KEY)),
                parseDoser(config.getJSONObject(CONFIG_DOSER_KEY)))
        }

        fun parseSmtp(smtp : JSONObject) : SmtpConfig {
            Log.d("parseSmtp", smtp.toString())
            return SmtpConfig(
                smtp.getString("enable"),
                smtp.getString("host"),
                smtp.getString("port"),
                smtp.getString("username"),
                smtp.getString("password"),
                smtp.getString("to"))
        }

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
                waterChange.getString("enable"),
                waterChange.getString("notify"),
                waterChange.getString("subscribes"))
        }

        fun buildMetrics(controllerType: String, metrics: ArrayList<Metric>) {
            //val items = mapOf(controllerType to )
            for(metric in metrics) {

            }
        }
    }
}
