package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Metric
import org.json.JSONArray

class ControllerParser {

    companion object {
        fun parse(json: String): ArrayList<Controller> {
            return parse(JSONArray(json))
        }

        fun parse(jsonControllers : JSONArray) : ArrayList<Controller> {
            var controllers = ArrayList<Controller>(jsonControllers.length())
            for (i in 0..jsonControllers.length() - 1) {
                val jsonController = jsonControllers.getJSONObject(i)

                Log.d("ControllerParser.parse", jsonController.toString())

                val id = jsonController.getInt("id")
                val orgId = jsonController.getInt("organizationId")
                val type = jsonController.getString("type")
                val description = jsonController.getString("description")
                val enable = jsonController.getBoolean("enable")
                val notify = jsonController.getBoolean("notify")
                val uri = jsonController.getString("uri")
                //val configs = jsonController.getJSONObject("configs")
                val hardwareVersion = jsonController.getString("hardwareVersion")
                val firmwareVersion = jsonController.getString("firmwareVersion")

                var parsedMetrics = ArrayList<Metric>()
                var parsedChannels = ArrayList<Channel>()
                if(!jsonController.isNull("metrics")) {
                    val metrics = jsonController.getJSONArray("metrics")
                    parsedMetrics = MetricParser.parse(metrics)
                }
                if(!jsonController.isNull("channels")) {
                    val channels = jsonController.getJSONArray("channels")
                    parsedChannels = ChannelParser.parse(channels)
                }

                controllers.add(Controller(id, orgId, type, description, enable, notify, uri, hardwareVersion, firmwareVersion, parsedMetrics, parsedChannels))
            }
            return controllers
        }
    }
}


data class Controller(val id: Int, val orgId: Int, val type: String, val description: String, val enabled: Boolean,
                      val notify: Boolean, val uri: String, val hardwareVersion: String, val firmwareVersion: String,
                      val metrics: List<Metric>, val channels: List<Channel>)