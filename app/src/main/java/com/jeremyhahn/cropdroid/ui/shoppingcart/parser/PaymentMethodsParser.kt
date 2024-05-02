package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.PaymentMethod
import org.json.JSONArray

class PaymentMethodsParser {
    companion object {
        fun parse(jsonPaymentMethonds: JSONArray): List<PaymentMethod> {
            val paymentMethods = ArrayList<PaymentMethod>()
            for (i in 0..<jsonPaymentMethonds.length()) {
                val jsonPaymentMethod = jsonPaymentMethonds.getJSONObject(i)
                val id = jsonPaymentMethod.getLong("id")
                val type = jsonPaymentMethod.getString("type")
                if (!jsonPaymentMethod.isNull("card")) {
                    val card = CreditCardParser.parse(jsonPaymentMethod.getJSONObject("card"))
                    paymentMethods.add(PaymentMethod(id, type, card))
                }
            }
            return paymentMethods
        }
    }
}
