package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Metric
import org.json.JSONArray
import org.json.JSONObject

class MetricParser {

    companion object {
        fun parse(json: String): ArrayList<Metric> {
            return parse(JSONArray(json))
        }
/*
        fun parse(jsonMetrics : JSONObject) : ArrayList<Metric> {
            return parse(jsonMetrics.getJSONObject("metrics"))
        }
*/
        fun parse(jsonMetrics : JSONArray) : ArrayList<Metric> {
            var metrics = ArrayList<Metric>(jsonMetrics.length())
            for (i in 0..jsonMetrics.length() - 1) {
                val jsonMetric = jsonMetrics.getJSONObject(i)

                Log.d("MetricParser.parse", jsonMetric.toString())

                metrics.add(MetricParser.parse(jsonMetric))
            }
            return metrics
        }

        fun parse(jsonMetric: JSONObject): Metric {
            val id = jsonMetric.getInt("id")
            val enabled = jsonMetric.getBoolean("enable")
            val notify = jsonMetric.getBoolean("notify")
            val key = jsonMetric.getString("key")
            val name = jsonMetric.getString("name")
            val unit = jsonMetric.getString("unit")
            val alarmLow = jsonMetric.getDouble("alarmLow")
            val alarmHigh = jsonMetric.getDouble("alarmHigh")
            val value = jsonMetric.getDouble("value")
            return Metric(id, key, name, enabled, notify, unit, alarmLow, alarmHigh, value)
        }
    }
}