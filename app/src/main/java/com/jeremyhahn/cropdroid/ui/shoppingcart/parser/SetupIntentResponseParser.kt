package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.SetupIntentResponse
import org.json.JSONObject

class SetupIntentResponseParser {
    companion object {
        fun parse(json: JSONObject): SetupIntentResponse {
            val customer = CustomerParser.parse(json.getJSONObject("customer"))
            val ephemeralKey = json.getString("ephemeral_key")
            val clientSecret = json.getString("client_secret")
            val publishableKey = json.getString("publishable_key")
            return SetupIntentResponse(customer, ephemeralKey, clientSecret, publishableKey)
        }
    }
}