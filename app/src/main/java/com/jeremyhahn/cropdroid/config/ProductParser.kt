package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.Algorithm
import com.jeremyhahn.cropdroid.ui.shoppingcart.Product
import org.json.JSONArray

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
                val price = jsonProduct.getDouble("price")
                val quantity = jsonProduct.getInt("quantity")
                products.add(Product(id, name, description, imageUrl, price, quantity))
            }
            return products
        }
    }
}