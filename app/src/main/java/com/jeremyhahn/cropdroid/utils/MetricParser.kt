package com.jeremyhahn.cropdroid.utils

import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import org.json.JSONArray
import org.json.JSONObject

class MetricParser {

    companion object {
        fun Parse(json: String): ArrayList<Metric> {
            return Parse(JSONArray(json))
        }

        fun Parse(jsonMetrics : JSONArray) : ArrayList<Metric> {
            var metrics = ArrayList<Metric>(jsonMetrics.length())
            for (i in 0..jsonMetrics.length() - 1) {
                val jsonMetric = jsonMetrics.getJSONObject(i)
                val id = jsonMetric.getString("id")
                val name = jsonMetric.getString("name")
                val display = jsonMetric.getString("display")
                val unit = jsonMetric.getString("unit")
                val enabled = jsonMetric.getString("enabled")
                val notify = jsonMetric.getString("notify")
                val alarmLow = jsonMetric.getString("alarmLow")
                val alarmHigh = jsonMetric.getString("alarmHigh")
                val value = jsonMetric.getString("value")
                metrics.add(Metric(id, name, display, unit, enabled, notify, alarmLow, alarmHigh, value))
            }
            return metrics
        }
    }
}