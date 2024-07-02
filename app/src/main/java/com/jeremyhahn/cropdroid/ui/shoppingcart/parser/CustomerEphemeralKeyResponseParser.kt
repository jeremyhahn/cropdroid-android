package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.CustomerEphemeralKeyResponse
import org.json.JSONObject

class CustomerEphemeralKeyResponseParser {
    companion object {
        fun parse(json: JSONObject): CustomerEphemeralKeyResponse {
            val customer = CustomerParser.parse(json.getJSONObject("customer"))
            val ephemeralKey = json.getString("ephemeral_key")
            return CustomerEphemeralKeyResponse(customer, ephemeralKey)
        }
    }
}
