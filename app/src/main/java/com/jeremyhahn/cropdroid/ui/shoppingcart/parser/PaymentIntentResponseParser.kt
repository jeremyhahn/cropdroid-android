package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.rest.PaymentIntentResponse
import org.json.JSONObject

class PaymentIntentResponseParser {
    companion object {
        fun parse(json: JSONObject): PaymentIntentResponse {
            val customer = CustomerParser.parse(json.getJSONObject("customer"))
            val invoiceId = json.getString("invoiceId")
            val paymentIntent = json.getString("paymentIntent")
            val clientSecret = json.getString("clientSecret")
            val ephemeralKey = json.getString("ephemeralKey")
            val publishableKey = json.getString("publishableKey")
            return PaymentIntentResponse(customer, invoiceId, paymentIntent, clientSecret, ephemeralKey, publishableKey)
        }
    }
}

