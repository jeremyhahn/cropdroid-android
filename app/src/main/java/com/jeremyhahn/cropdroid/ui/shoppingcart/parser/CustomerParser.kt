package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer
import org.json.JSONObject

class CustomerParser {
    companion object {
        fun parse(json: JSONObject): Customer {
            val id = json.getLong("id")
            val processorId = json.getString("processor_id")
            val description = json.getString("description")
            val name = json.getString("name")
            val email = json.getString("email")
            val phone = json.getString("phone")
            val address = if(json.isNull("address")) null else AddressParser.parse(
                json.getJSONObject(
                    "address"
                )
            )
            val shipping = if(json.isNull("shipping")) null else ShippingAddressParser.parse(
                json.getJSONObject(
                    "shipping"
                )
            )
            return Customer(id, processorId, description, name, email, phone, address, shipping)
        }
    }
}

