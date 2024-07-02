package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.CreditCard
import org.json.JSONObject

class CreditCardParser {
    companion object {
        fun parse(json: JSONObject): CreditCard {
            val id = json.getLong("id")
            val brand = json.getString("display_brand")
            val expMonth = json.getString("exp_month")
            val expYear = json.getString("exp_year")
            val last4 = json.getString("last4")
            val country = json.getString("country")
            val threeDSecureStorage = json.getBoolean("threeDSecureStorage")
            val checks = CreditCardCheckParser.parse(json.getJSONObject("checks"))
            return CreditCard(id, brand, country, expMonth, expYear, last4, threeDSecureStorage, checks)
        }
    }
}