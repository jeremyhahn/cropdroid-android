package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Condition
import org.json.JSONArray

class ConditionParser {

    companion object {
        fun parse(json: String): ArrayList<Condition> {
            return parse(JSONArray(json))
        }

        fun parse(jsonConditions : JSONArray) : ArrayList<Condition> {
            var conditions = ArrayList<Condition>(jsonConditions.length())
            for (i in 0..jsonConditions.length() - 1) {
                val jsonCondition = jsonConditions.getJSONObject(i)

                Log.d("ConditionParser.parse", jsonCondition.toString())

                val id = jsonCondition.getInt("id")
                val channelId = jsonCondition.getInt("channelId")
                val metricId = jsonCondition.getInt("metricId")
                val comparator = jsonCondition.getString("comparator")
                val threshold = jsonCondition.getDouble("threshold")
                conditions.add(Condition(id, channelId, metricId, comparator, threshold))
            }
            return conditions
        }
    }
}