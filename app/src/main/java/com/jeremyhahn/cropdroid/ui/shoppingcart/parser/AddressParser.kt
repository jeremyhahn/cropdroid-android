package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Address
import org.json.JSONObject

class AddressParser {
    companion object {
        fun parse(json: JSONObject): Address {
            val id = json.getLong("id")
            val line1 = json.getString("line1")
            val line2 = json.getString("line2")
            val city = json.getString("city")
            val state = json.getString("state")
            val postalCode = json.getString("postal_code")
            val country = json.getString("country")
            return Address(id, line1, line2, city, state, postalCode, country)
        }
    }
}
