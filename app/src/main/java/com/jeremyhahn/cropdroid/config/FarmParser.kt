package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.model.SmtpConfig
import com.jeremyhahn.cropdroid.model.Workflow
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder

class FarmParser {

    companion object {

        fun parse(json: String, orgId: Long, brief: Boolean): ArrayList<Farm> {
            return parse(JSONArray(json), orgId, brief)
        }

        fun parse(jsonFarm: JSONObject, orgId: Long, brief: Boolean) : Farm {
            Log.d("FarmParser.parse", jsonFarm.toString())
            var _orgId = orgId
            val id = jsonFarm.getLong("id")
            val name = jsonFarm.getString("name")
            var roles = ArrayList<String>(0)
            var workflows = ArrayList<Workflow>(0)

            if(!jsonFarm.isNull("roles")) {
                roles = RoleParser.parseStrings(jsonFarm.getJSONArray("roles"))
            }

            if(!jsonFarm.isNull("orgId")) {
                _orgId = jsonFarm.getLong("orgId")
            }

            if(brief) return Farm(id, _orgId, name)

            val interval = jsonFarm.getInt("interval")
            val mode = jsonFarm.getString("mode")
            val timezone = jsonFarm.getString("timezone")
            val devices = ControllerParser.parse(jsonFarm.getJSONArray("devices"))

            var smtpConfig = SmtpConfig()
            if(!jsonFarm.isNull("smtp")) {
                smtpConfig = SmtpParser.parse(jsonFarm.getJSONObject("smtp"))
            }

            if(!jsonFarm.isNull("workflows")) {
                workflows = WorkflowParser.parse(jsonFarm.getJSONArray("workflows"))
            }

            // TODO: Hacking in viewmodel values not returned by the FarmConfig websocket / ConfigManager.onMessage
            for(workflow in workflows) {

                for((i, step) in workflow.steps.withIndex()) {

                    if(step.channelName.isEmpty() || step.text.isEmpty()) {

                        var outerBreak = false
                        for (device in devices) {
                            if (device.id == step.deviceId) {
                                step.deviceType = device.type
                                for (channel in device.channels) {
                                    if (channel.id == step.channelId) {
                                        step.channelName = channel.name
                                        step.text = toTitle(device.type)!!.plus(" ").plus(channel.name)
                                                .plus(" ON for ").plus(step.duration)
                                                .plus("s, wait ").plus(step.wait)
                                        outerBreak = true
                                        break
                                    }
                                }
                            }
                            if (outerBreak) {
                                break
                            }
                        }
                    }
                }
            }

            Log.i("FarmParser.parse", "Parsing timezone: " + timezone)

            return Farm(id, orgId, mode, name, interval, timezone, smtpConfig, devices, roles, workflows)
        }

        fun parse(jsonFarms: JSONArray, orgId: Long, brief: Boolean) : ArrayList<Farm> {
            var farms = ArrayList<Farm>(jsonFarms.length())
            for (i in 0..jsonFarms.length() - 1) {
                val jsonFarm = jsonFarms.getJSONObject(i)
                farms.add(parse(jsonFarm, orgId, brief))
            }
            return farms
        }

        fun toTitle(text: String?): String? {
            if (text == null || text.isEmpty()) {
                return text
            }
            val converted = StringBuilder()
            var convertNext = true
            for (ch in text.toCharArray()) {
                var newCh: Char? = null
                if (Character.isSpaceChar(ch)) {
                    convertNext = true
                } else if (convertNext) {
                    newCh = Character.toTitleCase(ch)
                    convertNext = false
                } else {
                    newCh = Character.toLowerCase(ch)
                }
                converted.append(newCh)
            }
            return converted.toString()
        }
    }
}
