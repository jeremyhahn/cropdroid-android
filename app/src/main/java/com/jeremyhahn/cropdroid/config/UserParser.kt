package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.RoleConfig
import com.jeremyhahn.cropdroid.model.UserConfig
import org.json.JSONArray
import org.json.JSONObject

class UserParser {
    companion object {
        fun parse(jsonUsers: JSONArray) : ArrayList<UserConfig> {
            Log.d("[UserParser.parse] jsonUsers: ", jsonUsers.toString())
            var users = ArrayList<UserConfig>(jsonUsers.length())
            for (i in 0..jsonUsers.length()-1) {
                val jsonUser = jsonUsers.getJSONObject(i)

                val id = jsonUser.getLong("id")
                val email = jsonUser.getString("email")
                val password = jsonUser.getString("password")

                val jsonRoles = jsonUser.getJSONArray("roles")
                val roles = RoleParser.parse(jsonRoles)

                users.add(UserConfig(id, email, password, roles))
            }
            return users
        }
    }
}
