package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer

data class SetupIntentResponse(
    val customer: Customer,
    val ephemeralKey: String,
    val clientSecret: String,
    val publishableKey: String
)
