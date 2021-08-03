package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_CONTROLLERS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARMS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_INTERVAL_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORGS_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_SMTP_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_TIMEZONE_KEY
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.utils.SmtpParser
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ConfigParser {

    companion object {

        fun parse(json: String): ServerConfig {
            return parse(
                JSONObject(json)
            )
        }

        fun parse(config: JSONObject): ServerConfig {
            return ServerConfig(
                //config.getString(CONFIG_NAME_KEY),
                "",
                config.getString(CONFIG_INTERVAL_KEY),
                config.getString(CONFIG_TIMEZONE_KEY),
                config.getString(CONFIG_MODE_KEY),
                //parseSmtp(config.getJSONObject(CONFIG_SMTP_KEY)),
                SmtpConfig("false", "localhost", "25", "", "", "nobody@blackhole.com"),
                parseOrganizations(config.getJSONArray(CONFIG_ORGS_KEY)),
                parseFarms(config.getJSONArray(CONFIG_FARMS_KEY)))
        }

        fun parseOrganizations(jsonOrgs: JSONArray) : ArrayList<Organization> {
            Log.d("parseOrganizations", jsonOrgs.toString())

            var orgs = ArrayList<Organization>(jsonOrgs.length())
            for (i in 0..jsonOrgs.length() - 1) {

                val jsonOrg = jsonOrgs.getJSONObject(i)

                /*
                Log.d("ConfigParser.parseOrganizations", jsonOrg.toString())

                val id = jsonOrg.getLong("id")
                val name = jsonOrg.getString("name")
                val farms = parseFarms(jsonOrg.getJSONArray(CONFIG_FARMS_KEY))
                val roles = RoleParser.parse(jsonOrg.getJSONArray(CONFIG_ROLES_KEY))
                //val roles = ArrayList<String>(0)
                //roles.add("admin")

                var license = jsonOrg.getString("license")

                orgs.add(Organization(id, name, farms, license, roles))
                 */
                orgs.add(
                    OrganizationParser.parse(
                        jsonOrg,
                        true
                    )
                )
            }
            return orgs
        }

        fun parseFarms(jsonFarms: JSONArray): ArrayList<Farm> {

            Log.d("parseFarms", jsonFarms.toString())

            var farms = ArrayList<Farm>(jsonFarms.length())
            for (i in 0..jsonFarms.length() - 1) {

                val jsonFarm = jsonFarms.getJSONObject(i)

                Log.d("ConfigParser.parseFarms", jsonFarm.toString())

                val id = jsonFarm.getLong("id")
                val orgId = jsonFarm.getLong("orgId")
                val mode = jsonFarm.getString("mode")
                val name = jsonFarm.getString("name")
                val interval = jsonFarm.getInt("interval")
                val controllers =
                    parseControllers(
                        jsonFarm.getJSONArray(CONFIG_CONTROLLERS_KEY)
                    )
                val timezone = jsonFarm.getString("timezone")

                val smtp =
                    SmtpParser.parse(
                        jsonFarm.getJSONObject(CONFIG_SMTP_KEY)
                    )

                //var jsonRoles = jsonFarm.getJSONArray(CONFIG_ROLES_KEY)
                //val roles = RoleParser.parse(jsonRoles)
                val roles = ArrayList<String>(0)
                roles.add("admin")

                val workflows = WorkflowParser.parse(jsonFarm.getJSONArray("workflows"))

                farms.add(Farm(id, orgId, mode, name, interval, timezone, smtp, controllers, roles, workflows))
            }
            return farms
        }

        fun parseControllers(jsonControllers: JSONArray): ArrayList<Controller> {
            Log.d("parseControllers", jsonControllers.toString())

            var controllers = ArrayList<Controller>(jsonControllers.length())
            for (i in 0..jsonControllers.length() - 1) {

                val jsonChannel = jsonControllers.getJSONObject(i)

                Log.d("ConfigParser.parseControllers", jsonChannel.toString())

                val jsonConfigs = jsonChannel.getJSONObject("configs")
                val configs = HashMap<String, Any>(jsonConfigs.length())
                for ((i, k) in jsonConfigs.keys().withIndex()) {
                    val v = jsonConfigs.getString(k)
                    if(v.toLowerCase().equals("true") || v.toLowerCase().equals("false")) {
                        configs.put(k, v.toBoolean())
                    } else {
                        configs.put(k, v)
                    }
                    Log.i("ConfigParser.parseControllers", "Putting config -- Key: " + k + ", value: " + v)
                }
                val id = jsonChannel.getLong("id")
               // val orgId = jsonChannel.getInt("orgId")
                val type = jsonChannel.getString("type")
                val description = jsonChannel.getString("description")
                //val enabled = jsonChannel.getBoolean("enable")
                //val notify = jsonChannel.getBoolean("notify")
                //val uri = jsonChannel.getString("uri")
                val hardwareVersion = jsonChannel.getString("hwVersion")
                val firmwareVersion = jsonChannel.getString("fwVersion")
                val metrics =
                    MetricParser.parse(
                        jsonChannel.getJSONArray("metrics")
                    )
                val channels =
                    ChannelParser.parse(
                        jsonChannel.getJSONArray("channels")
                    )
                controllers.add(Controller(id, type, description, hardwareVersion, firmwareVersion, configs, metrics, channels))
            }
            return controllers
        }
//
//        fun parseWorkflows(jsonWorkflows: JSONArray): ArrayList<Workflow> {
//
//            var workflows = ArrayList<Workflow>(jsonWorkflows.length())
//            for (i in 0..jsonWorkflows.length() - 1) {
//
//                val jsonWorkflow = jsonWorkflows.getJSONObject(i)
//                Log.d("ConfigParser.parseWorkflows", jsonWorkflows.toString())
//
//                val id = jsonWorkflow.getLong("id")
//                val farmId = jsonWorkflow.getLong("farm_id")
//                val name = jsonWorkflow.getString("name")
//                //val conditions = jsonWorkflow.getJSONObject("conditions")
//                //val schedules = jsonWorkflow.getJSONObject("schedules")
//                val steps = parseWorkflowSteps(jsonWorkflow.getJSONArray("steps"))
//
//                val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
//                var lastCompletedCalendar: Calendar? = null
//                if(!jsonWorkflow.isNull("lastCompleted")) {
//                    val lastCompleted = jsonWorkflow.getString("lastCompleted")
//                    lastCompletedCalendar = Calendar.getInstance()
//                    try {
//                        lastCompletedCalendar.time = formatter.parse(lastCompleted)
//                    } catch (e: ParseException) {
//                        Log.e("ConfigParser.parseWorkflows", "lastCompleted=" + lastCompleted + ", error=" + e.message)
//                    }
//                }
//
//                workflows.add(Workflow(id, farmId, name, ArrayList(), ArrayList(), steps, lastCompletedCalendar))
//            }
//            return workflows
//        }
//
//        fun parseWorkflowSteps(jsonWorkflowSteps: JSONArray): ArrayList<WorkflowStep> {
//
//            var workflowSteps = ArrayList<WorkflowStep>(jsonWorkflowSteps.length())
//            for (i in 0..jsonWorkflowSteps.length() - 1) {
//
//                val jsonWorkflowStep = jsonWorkflowSteps.getJSONObject(i)
//                Log.d("ConfigParser.parseWorkflowSteps", jsonWorkflowStep.toString())
//
//                val id = jsonWorkflowStep.getLong("id")
//                val workflowId = jsonWorkflowStep.getLong("workflow_id")
//                val deviceId = jsonWorkflowStep.getLong("device_id")
//                val channelId = jsonWorkflowStep.getLong("channel_id")
//                val webhook = jsonWorkflowStep.getString("webhook")
//                val duration = jsonWorkflowStep.getInt("duration")
//                val wait = jsonWorkflowStep.getInt("wait")
//                val state = jsonWorkflowStep.getInt("state")
//
//                workflowSteps.add(WorkflowStep(id, workflowId, deviceId, channelId, webhook, duration, wait, state))
//            }
//            return workflowSteps
//        }
    }
}