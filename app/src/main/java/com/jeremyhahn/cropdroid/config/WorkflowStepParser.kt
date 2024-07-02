package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.WorkflowStep
import org.json.JSONArray

class WorkflowStepParser {

    companion object {
        fun parse(json: String): ArrayList<WorkflowStep> {
            return parse(
                JSONArray(json)
            )
        }

        fun parse(jsonWorkflowSteps : JSONArray) : ArrayList<WorkflowStep> {
            var workflowSteps = ArrayList<WorkflowStep>(jsonWorkflowSteps.length())
            for (i in 0..jsonWorkflowSteps.length() - 1) {
                val step = jsonWorkflowSteps.getJSONObject(i)

                Log.d("WorkflowStepParser.parse", step.toString())

                val id = step.getLong("id")
                val workflowId = if(step.isNull("workflowId")) step.getLong("workflow_id") else step.getLong("workflowId")
                val deviceId = if(step.isNull("deviceId")) step.getLong("device_id") else step.getLong("deviceId")
                val deviceType = if(step.isNull("deviceType")) "" else step.getString("deviceType")
                val channelId = if(step.isNull("channelId")) step.getLong("channel_id") else step.getLong("channelId")
                val channelName = if(step.isNull("channelName")) "" else step.getString("channelName")
                val webhook = step.getString("webhook")
                val duration = step.getInt("duration")
                val wait = step.getInt("wait")
                val state = step.getInt("state")
                val text = if(!step.isNull("text")) step.getString("text") else ""

                workflowSteps.add(WorkflowStep(id, workflowId, deviceId, deviceType, channelId, channelName, webhook, duration, wait, state, text))
            }
            return workflowSteps
        }
    }

}

