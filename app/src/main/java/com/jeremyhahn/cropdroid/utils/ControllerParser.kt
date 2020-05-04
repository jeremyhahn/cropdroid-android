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

                val jsonConfigs = jsonController.getJSONArray("configs")
                val configs = HashMap<String, String>(jsonConfigs.length())
                for (i in 0 until jsonConfigs.length()) {
                    val jsonObj: JSONObject = jsonConfigs.getJSONObject(i)
                    val k = jsonObj.keys().next()
                    val v = jsonObj.getString(k)
                    configs.put(k, v)
                    Log.i("ControllerParser.parseConfigs", "Putting config -- Key: " + k + ", value: " + v)
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
                controllers.add(Controller(id, orgId, type, description, hardwareVersion, firmwareVersion, configs, parsedMetrics, parsedChannels))
            }
            return controllers
        }
    }
}
