package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product

data class CreateInvoiceRequest(val description: String, val products: ArrayList<Product>)