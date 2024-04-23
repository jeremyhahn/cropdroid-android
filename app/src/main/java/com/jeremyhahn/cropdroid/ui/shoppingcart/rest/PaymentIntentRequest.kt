package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

data class PaymentIntentRequest(val customerId: String, val amount: String, val currencyCode: String)

