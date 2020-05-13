package com.jeremyhahn.cropdroid.utils

import android.util.Log
import org.json.JSONArray

class RoleParser {

    companion object {
        fun parse(jsonRoles : JSONArray) : ArrayList<String> {
            Log.d("[RoleParser.parse] jsonRoles: ", jsonRoles.toString())
            var roles = ArrayList<String>(jsonRoles.length())
            for (i in 0..jsonRoles.length() - 1) {
                roles.add(jsonRoles.getString(i))
            }
            return roles
        }
    }
}