package com.jeremyhahn.cropdroid.ui.shoppingcart.parser

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.TaxRate
import org.json.JSONArray
import org.json.JSONObject

class TaxRateParser {
    companion object {
        fun parse(jsonTaxRates: JSONArray): List<TaxRate> {
            var taxRates = ArrayList<TaxRate>(jsonTaxRates.length())
            for (i in 0..<jsonTaxRates.length()) {
                val jsonTaxRate = jsonTaxRates.getJSONObject(i)
                taxRates.add(parse(jsonTaxRate))
            }
            return taxRates
        }
        fun parse(jsonTaxRate: JSONObject): TaxRate {
            val id = jsonTaxRate.getString("id")
            val displayName = jsonTaxRate.getString("display_name")
            val description = jsonTaxRate.getString("description")
            val state = jsonTaxRate.getString("state")
            val country = jsonTaxRate.getString("country")
            val inclusive = jsonTaxRate.getBoolean("inclusive")
            val jurisdiction = jsonTaxRate.getString("jurisdiction")
            val percentage = jsonTaxRate.getDouble("percentage")
            return TaxRate(id, displayName, description, state, country, inclusive, jurisdiction, percentage)
        }
    }
}
