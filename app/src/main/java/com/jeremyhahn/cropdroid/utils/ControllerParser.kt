package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Metric
import org.json.JSONArray
import org.json.JSONObject

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
                val orgId = jsonController.getInt("orgId")
                val type = jsonController.getString("type")
                val description = jsonController.getString("description")
                //val enable = jsonController.getBoolean("enable")
                //val notify = jsonController.getBoolean("notify")
                //val uri = jsonController.getString("uri")

                //val configs = jsonController.getJSONObject("configs")

                val jsonConfigs = jsonController.getJSONObject("configs")
                val configs = HashMap<String, Any>(jsonConfigs.length())

                for ((i, k) in jsonConfigs.keys().withIndex()) {
                    val v = jsonConfigs.getString(k)
                    if (v.toLowerCase().equals("true") || v.toLowerCase().equals("false")) {
                        configs.put(k, v.toBoolean())
                    } else {
                        configs.put(k, v)
                    }
                    Log.i("ControllerParser.parse", "Putting config -- Key: " + k + ", value: " + v)
                }

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

                //controllers.add(Controller(id, orgId, type, description, enable, notify, uri, hardwareVersion, firmwareVersion, parsedMetrics, parsedChannels))
                val controller = Controller(id, orgId, type, description, hardwareVersion, firmwareVersion, configs, parsedMetrics, parsedChannels)

                Log.d("RETURNING (controllerparser)", controller.toString())

                controllers.add(controller)
            }
            return controllers
        }
    }
}
