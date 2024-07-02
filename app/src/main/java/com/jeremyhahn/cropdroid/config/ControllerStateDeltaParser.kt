package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.ControllerStateDelta
import org.json.JSONObject
import java.lang.Integer.parseInt

class ControllerStateDeltaParser {

    companion object {

        @Synchronized
        fun parse(json: String): ControllerStateDelta {
            return parse(JSONObject(json))
        }

        fun parse(jsonController: JSONObject): ControllerStateDelta {

            for ((i, controllerType) in jsonController.keys().withIndex()) {

                val jsonController = jsonController.getJSONObject(controllerType)

                Log.d("ControllerStateDeltaParser.parse", jsonController.toString())

                val jsonMetrics = jsonController.getJSONObject("metrics")
                val metrics = HashMap<String, Double>(jsonMetrics.length())
                for ((i, k) in jsonMetrics.keys().withIndex()) {
                    val v = jsonMetrics.getString(k)
                    metrics.put(k, v.toDouble())
                    Log.i("ControllerStateDeltaParser.parse", "Parsing metric - Key: " + k + ", value: " + v)
                }

                val jsonChannels = jsonController.getJSONObject("channels")
                val channels = HashMap<Int, Int>(jsonChannels.length())
                for ((i, k) in jsonChannels.keys().withIndex()) {
                    val v = jsonChannels.getString(k)
                    channels.put(parseInt(k), v.toInt())
                    Log.i("ControllerStateDeltaParser.parse", "Parsing channel delta - channel id: " + k + ", value: " + v)
                }

                //val timestamp = jsonController.getString("timestamp")
                val timestamp = ""

                return ControllerStateDelta(controllerType, metrics, channels, timestamp)
            }
            return ControllerStateDelta()
        }
    }
}
