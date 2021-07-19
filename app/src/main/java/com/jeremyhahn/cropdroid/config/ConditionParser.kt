package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.Condition
import org.json.JSONArray
import java.math.BigInteger

class ConditionParser {

    companion object {
        fun parse(json: String): ArrayList<Condition> {
            return parse(
                JSONArray(json)
            )
        }

        fun parse(jsonConditions : JSONArray) : ArrayList<Condition> {
            var conditions = ArrayList<Condition>(jsonConditions.length())
            for (i in 0..jsonConditions.length() - 1) {
                val jsonCondition = jsonConditions.getJSONObject(i)

                Log.d("ConditionParser.parse", jsonCondition.toString())

                val id = jsonCondition.getString("id")
                val controllerType = jsonCondition.getString("deviceType")
                //val metric = MetricParser.parse(jsonCondition.getJSONObject("metric"))
                val metricId = jsonCondition.getLong("metricId")
                val channelId = jsonCondition.getInt("channelId")
                val metricName = jsonCondition.getString("metricName")
                val comparator = jsonCondition.getString("comparator")
                val threshold = jsonCondition.getDouble("threshold")
                val text = jsonCondition.getString("text")
                conditions.add(Condition(id, controllerType, metricId, metricName, channelId, comparator, threshold, text))
            }
            return conditions
        }
    }
}
