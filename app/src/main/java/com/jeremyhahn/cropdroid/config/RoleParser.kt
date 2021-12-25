package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.RoleConfig
import org.json.JSONArray

class RoleParser {
    companion object {
        fun parse(jsonRoles: JSONArray) : ArrayList<RoleConfig> {
            Log.d("[RoleParser.parse] jsonRoles: ", jsonRoles.toString())
            var roles = ArrayList<RoleConfig>(jsonRoles.length())
            for (i in 0..jsonRoles.length()-1) {
                val jsonRole = jsonRoles.getJSONObject(i)
                val id = jsonRole.getLong("id")
                val name = jsonRole.getString("name")
                roles.add(RoleConfig(id, name))
            }
            return roles
        }
        fun parseStrings(jsonRoles : JSONArray) : ArrayList<String> {
            Log.d("[RoleParser.parse] jsonRoles: ", jsonRoles.toString())
            var roles = ArrayList<String>(jsonRoles.length())
            for (i in 0..jsonRoles.length() - 1) {
                roles.add(jsonRoles.getString(i))
            }
            return roles
        }
    }
}