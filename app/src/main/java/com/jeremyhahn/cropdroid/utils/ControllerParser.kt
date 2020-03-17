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

                controllers.add(Controller(id, orgId, type, description, hardwareVersion, firmwareVersion, parsedMetrics, parsedChannels))
            }
            return controllers
        }
    }
}
