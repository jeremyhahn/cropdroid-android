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

                val id = jsonMetric.getString("id")
                val name = jsonMetric.getString("name")
                val enabled = jsonMetric.getString("enable")
                val notify = jsonMetric.getString("notify")
                val display = jsonMetric.getString("display")
                val unit = jsonMetric.getString("unit")
                val alarmLow = jsonMetric.getString("alarmLow")
                val alarmHigh = jsonMetric.getString("alarmHigh")
                val value = jsonMetric.getString("value")
                metrics.add(Metric(id, name, enabled, notify, display, unit, alarmLow, alarmHigh, value))
            }
            return metrics
        }
    }
}