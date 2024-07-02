package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.*
import org.json.JSONObject

class ControllerStateParser {

    companion object {

        @Synchronized
        fun parse(json: String): ControllerState {
            return parse(JSONObject(json))
        }

        fun parse(jsonController: JSONObject): ControllerState {

            for ((i, controllerType) in jsonController.keys().withIndex()) {

                val jsonController = jsonController.getJSONObject(controllerType)

                Log.d("ControllerStateParser.parse", jsonController.toString())

                val jsonMetrics = jsonController.getJSONObject("metrics")
                val metrics = HashMap<String, Double>(jsonMetrics.length())
                for ((i, k) in jsonMetrics.keys().withIndex()) {
                    val v = jsonMetrics.getString(k)
                    metrics.put(k, v.toDouble())
                    Log.i(
                        "ControllerStateParser.parse",
                        "Parsing metric - Key: " + k + ", value: " + v
                    )
                }

                val jsonChannels = jsonController.getJSONArray("channels")
                val channels = ArrayList<Int>()
                for (i in 0..jsonChannels.length() - 1) {
                    channels.add(jsonChannels.getInt(i))
                }

                //val timestamp = jsonController.getString("timestamp")
                val timestamp = ""

                return ControllerState(controllerType, metrics, channels, timestamp)
            }
            return ControllerState()
        }
    }
}
