package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.CreditCardChecks
import org.json.JSONObject

class CreditCardCheckParser {
    companion object {
        fun parse(json: JSONObject): CreditCardChecks {
            val addressLine1 = json.getString("address_line1_check")
            val addressPostalCode = json.getString("address_postal_code_check")
            val cvc = json.getString("cvc_check")
            return CreditCardChecks(addressLine1, addressPostalCode, cvc)
        }
    }
}