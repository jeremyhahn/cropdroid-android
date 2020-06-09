package com.jeremyhahn.cropdroid.utils

import com.jeremyhahn.cropdroid.model.Metric
import org.json.JSONArray
import org.json.JSONObject

class MetricParser {

    companion object {
        fun parse(json: String): ArrayList<Metric> {
            return parse(JSONArray(json))
        }

        fun parse(jsonMetrics : JSONArray) : ArrayList<Metric> {
            var metrics = ArrayList<Metric>(jsonMetrics.length())
            for (i in 0..jsonMetrics.length() - 1) {
                val jsonMetric = jsonMetrics.getJSONObject(i)

                //Log.d("MetricParser.parse", jsonMetric.toString())

                metrics.add(MetricParser.parse(jsonMetric))
            }
            return metrics
        }

        fun parse(jsonMetric: JSONObject): Metric {
            val id = jsonMetric.getLong("id")
            val controllerId = if(!jsonMetric.isNull("controller_io")) jsonMetric.getLong("controller_id") else 0
            val datatype = jsonMetric.getInt("datatype")
            val enabled = jsonMetric.getBoolean("enable")
            val notify = jsonMetric.getBoolean("notify")
            val key = jsonMetric.getString("key")
            val name = jsonMetric.getString("name")
            val unit = jsonMetric.getString("unit")
            val alarmLow = jsonMetric.getDouble("alarmLow")
            val alarmHigh = jsonMetric.getDouble("alarmHigh")
            var value: Double = 0.0
            if(!jsonMetric.isNull("value")) {
                value = jsonMetric.getDouble("value")
            }
            return Metric(id, controllerId, datatype, key, name, enabled, notify, unit, alarmLow, alarmHigh, value)
        }
    }
}