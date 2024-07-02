package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product
import org.json.JSONArray
import org.json.JSONObject

class ProductParser {

    companion object {
        fun parse(json: String): ArrayList<Product> {
            return parse(
                JSONArray(json)
            )
        }

        fun parse(jsonProducts : JSONArray) : ArrayList<Product> {
            var products = ArrayList<Product>(jsonProducts.length())
            for (i in 0..<jsonProducts.length()) {
                val jsonProduct = jsonProducts.getJSONObject(i)

                Log.d("ProductParser.parse", jsonProduct.toString())

                val id = jsonProduct.getString("id")
                val name = jsonProduct.getString("name")
                val description = jsonProduct.getString("description")
                val imageUrl = jsonProduct.getString("imageUrl")
                val price = jsonProduct.getLong("price")
                val quantity = jsonProduct.getInt("quantity")
                val metadata = jsonProduct.getJSONObject("metadata").toMap()
                products.add(Product(id, name, description, imageUrl, price, quantity, metadata))
            }
            return products
        }

        fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
            when (val value = this[it])
            {
                is JSONArray ->
                {
                    val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                    JSONObject(map).toMap().values.toList()
                }
                is JSONObject -> value.toMap()
                JSONObject.NULL -> null
                else            -> value
            }
        }
    }
}