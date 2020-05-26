package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Farm
import org.json.JSONArray

class FarmParser {

    companion object {

        fun parse(json: String, orgId: Int): ArrayList<Farm> {
            return parse(JSONArray(json), orgId)
        }

        fun parse(jsonFarms: JSONArray, orgId: Int) : ArrayList<Farm> {
            var farms = ArrayList<Farm>(jsonFarms.length())
            for (i in 0..jsonFarms.length() - 1) {
                val jsonFarm = jsonFarms.getJSONObject(i)

                Log.d("FarmParser.parse", jsonFarm.toString())

                val id = jsonFarm.getLong("id")
                val name = jsonFarm.getString("name")
                val interval = jsonFarm.getInt("interval")
                val mode = jsonFarm.getString("mode")

                val controllers = ArrayList<Controller>(0)

                var roles = ArrayList<String>(0)
                if(!jsonFarm.isNull("roles")) {
                    roles = RoleParser.parse(jsonFarm.getJSONArray("roles"))
                }
                farms.add(Farm(id, orgId, mode, name, interval, controllers, roles))

            }
            return farms
        }
    }
}