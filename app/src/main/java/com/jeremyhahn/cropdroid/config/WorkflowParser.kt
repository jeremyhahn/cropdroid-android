package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.model.Schedule
import com.jeremyhahn.cropdroid.model.Workflow
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WorkflowParser {

    companion object {
        fun parse(json: String): ArrayList<Workflow> {
            return parse(JSONArray(json))
        }

        fun parse(jsonWorkflows : JSONArray) : ArrayList<Workflow> {
            var workflowSteps = ArrayList<Workflow>(jsonWorkflows.length())
            for (i in 0..jsonWorkflows.length() - 1) {
                val jsonWorkflows = jsonWorkflows.getJSONObject(i)

                Log.d("WorkflowParser.parse", jsonWorkflows.toString())

                val id = jsonWorkflows.getLong("id")
                val farmId = if(jsonWorkflows.isNull("farmId")) jsonWorkflows.getLong("farm_id") else jsonWorkflows.getLong("farmId")

                val name = jsonWorkflows.getString("name")

                val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
                var lastCompletedCalendar: Calendar? = null
                if(!jsonWorkflows.isNull("lastCompleted")) {
                    val lastCompleted = jsonWorkflows.getString("lastCompleted")
                    lastCompletedCalendar = Calendar.getInstance()
                    try {
                        lastCompletedCalendar.time = formatter.parse(lastCompleted)
                    } catch (e: ParseException) {
                        Log.e(
                            "ScheduleParser.parse",
                            "lastCompleted=" + lastCompleted + ", error=" + e.message
                        )
                    }
                }

                var conditions = ArrayList<Condition>()
                var schedules = ArrayList<Schedule>()
                if(!jsonWorkflows.isNull("conditions")) {
                    conditions = ConditionParser.parse(jsonWorkflows.getJSONArray("conditions"))
                }
                if(!jsonWorkflows.isNull("schedules")) {
                    val schedules = ScheduleParser.parse(jsonWorkflows.getJSONArray("schedules"))
                }
                val steps = WorkflowStepParser.parse(jsonWorkflows.getJSONArray("steps"))

                workflowSteps.add(Workflow(id, farmId, name, conditions, schedules, steps, lastCompletedCalendar))
            }
            return workflowSteps
        }
    }
}
