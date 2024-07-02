package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.*
import org.json.JSONObject

class FarmStateParser {

    companion object {

        @Synchronized fun parse(json: String): FarmState {
            return parse(JSONObject(json))
        }

        fun parse(jsonControllers : JSONObject) : FarmState {

            var state = HashMap<String, ControllerState>(jsonControllers.length())
            var jsonControllers = jsonControllers.getJSONObject("devices")

            //jsonControllers.keys().forEachRemaining { key ->
            for ((i, controllerType) in jsonControllers.keys().withIndex()) {

                val jsonController = jsonControllers.getJSONObject(controllerType)

                Log.d("FarmStateParser.parse", jsonController.toString())

                val jsonMetrics = jsonController.getJSONObject("metrics")
                val metrics = HashMap<String, Double>(jsonMetrics.length())
                for ((i, k) in jsonMetrics.keys().withIndex()) {
                    val v = jsonMetrics.getString(k)
                    metrics.put(k, v.toDouble())
                    Log.i("FarmStateParser.parse", "Parsing metric - Key: " + k + ", value: " + v)
                }

                val jsonChannels = jsonController.getJSONArray("channels")
                val channels = ArrayList<Int>()
                for (i in 0..jsonChannels.length() - 1) {
                    channels.add(jsonChannels.getInt(i))
                }

                //val timestamp = jsonController.getString("timestamp")
                val timestamp = ""

                state.put(controllerType, ControllerState(controllerType, metrics, channels, timestamp))
            }
            return FarmState(state)
        }
    }
}
