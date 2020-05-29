package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Farm
import org.json.JSONArray
import org.json.JSONObject

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

            if(!jsonFarm.isNull("roles")) {
                roles = RoleParser.parse(jsonFarm.getJSONArray("roles"))
            }

            if(!jsonFarm.isNull("orgId")) {
                _orgId = jsonFarm.getLong("orgId")
            }

            if(brief) return Farm(id, _orgId, name)

            val interval = jsonFarm.getInt("interval")
            val mode = jsonFarm.getString("mode")
            val timezone = jsonFarm.getString("timezone")
            val controllers = ControllerParser.parse(jsonFarm.getJSONArray("controllers"))
            val smtpConfig = SmtpParser.parse(jsonFarm.getJSONObject("smtp"))

            return Farm(id, orgId, mode, name, interval, timezone, smtpConfig, controllers, roles)
        }

        fun parse(jsonFarms: JSONArray, orgId: Long, brief: Boolean) : ArrayList<Farm> {
            var farms = ArrayList<Farm>(jsonFarms.length())
            for (i in 0..jsonFarms.length() - 1) {

                val jsonFarm = jsonFarms.getJSONObject(i)
                farms.add(parse(jsonFarm, orgId, brief))
            }
            return farms
        }
    }
}
