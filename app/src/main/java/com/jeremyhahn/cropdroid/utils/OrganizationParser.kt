package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Organization
import org.json.JSONArray

class OrganizationParser {

    companion object {
        fun parse(json: String): ArrayList<Organization> {
            return parse(JSONArray(json))
        }

        fun parse(jsonOrgs: JSONArray) : ArrayList<Organization> {
            var orgs = ArrayList<Organization>(jsonOrgs.length())
            for (i in 0..jsonOrgs.length() - 1) {
                val jsonOrg = jsonOrgs.getJSONObject(i)

                Log.d("OrganizationParser.parse", jsonOrg.toString())

                val orgId = jsonOrg.getInt("id")
                val name = jsonOrg.getString("name")
                val farms = FarmParser.parse(jsonOrg.getJSONArray("farms"), orgId)
                val roles = RoleParser.parse(jsonOrg.getJSONArray("roles"))
                orgs.add(Organization(orgId, name, farms, "", roles))
            }
            return orgs
        }
    }
}