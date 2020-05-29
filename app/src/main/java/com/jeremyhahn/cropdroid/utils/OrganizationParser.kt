package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Organization
import org.bouncycastle.asn1.x500.style.RFC4519Style.name
import org.json.JSONArray
import org.json.JSONObject

class OrganizationParser {

    companion object {
        fun parse(json: String, brief: Boolean): ArrayList<Organization> {
            return parse(JSONArray(json), brief)
        }

        fun parse(jsonOrg: JSONObject, brief: Boolean): Organization {
            Log.d("OrganizationParser.parse", jsonOrg.toString())
            val orgId = jsonOrg.getLong("id")
            val name = jsonOrg.getString("name")
            val farms = FarmParser.parse(jsonOrg.getJSONArray("farms"), orgId, brief)
            var roles = RoleParser.parse(jsonOrg.getJSONArray("roles"))
            return Organization(orgId, name, farms, "", roles)
        }

        fun parse(jsonOrgs: JSONArray, brief: Boolean) : ArrayList<Organization> {
            var orgs = ArrayList<Organization>(jsonOrgs.length())
            for (i in 0..jsonOrgs.length() - 1) {
                val jsonOrg = jsonOrgs.getJSONObject(i)
                orgs.add(parse(jsonOrg, brief))
            }
            return orgs
        }
    }
}