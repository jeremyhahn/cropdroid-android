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
import com.jeremyhahn.cropdroid.utils.MetricParser
import com.jeremyhahn.cropdroid.utils.SmtpParser
import org.json.JSONArray
import org.json.JSONObject

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

                farms.add(Farm(id, orgId, mode, name, interval, timezone, smtp, controllers, roles))
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
                //val hardwareVersion = jsonChannel.getString("hardwareVersion")
                //val firmwareVersion = jsonChannel.getString("firmwareVersion")
                val metrics =
                    MetricParser.parse(
                        jsonChannel.getJSONArray("metrics")
                    )
                val channels =
                    ChannelParser.parse(
                        jsonChannel.getJSONArray("channels")
                    )
                //controllers.add(Controller(id, orgId, type, description, enabled, notify, uri, hardwareVersion, firmwareVersion, metrics, channels))
                //controllers.add(Controller(id, orgId, type, description, hardwareVersion, firmwareVersion, configs, metrics, channels))
                controllers.add(Controller(id, type, description, "", "", configs, metrics, channels))
            }
            return controllers
        }
    }
}