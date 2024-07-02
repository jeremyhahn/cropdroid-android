package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer

data class PaymentIntentResponse(
    val customer: Customer,
    val invoiceId: String,
    val paymentIntent: String,
    val clientSecret: String,
    val ephemeralKey: String,
    val publishableKey: String)
