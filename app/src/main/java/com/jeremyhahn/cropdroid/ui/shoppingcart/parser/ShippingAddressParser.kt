package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.ShippingAddress
import org.json.JSONObject

class ShippingAddressParser {
    companion object {
        fun parse(json: JSONObject): ShippingAddress {
            val id = json.getLong("id")
            val name = json.getString("name")
            val phone = json.getString("phone")
            val address = AddressParser.parse(json.getJSONObject("address"))
            return ShippingAddress(id, name, phone, address)
        }
    }
}