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

        fun parse(jsonMetrics : JSONObject) : ArrayList<Metric> {
            return parse(jsonMetrics.getJSONObject("metrics"))
        }

        fun parse(jsonMetrics : JSONArray) : ArrayList<Metric> {
            var metrics = ArrayList<Metric>(jsonMetrics.length())
            for (i in 0..jsonMetrics.length() - 1) {
                val jsonMetric = jsonMetrics.getJSONObject(i)

                Log.d("MetricParser.parse", jsonMetric.toString())

                val id = jsonMetric.getInt("id")
                val name = jsonMetric.getString("name")
                val display = jsonMetric.getString("display")
                val enabled = jsonMetric.getBoolean("enable")
                val notify = jsonMetric.getBoolean("notify")
                val unit = jsonMetric.getString("unit")
                val alarmLow = jsonMetric.getDouble("alarmLow")
                val alarmHigh = jsonMetric.getDouble("alarmHigh")
                val value = jsonMetric.getDouble("value")
                metrics.add(Metric(id, name, display, enabled, notify, unit, alarmLow, alarmHigh, value))
            }
            return metrics
        }
    }
}