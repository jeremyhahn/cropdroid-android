package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Customer

data class CustomerEphemeralKeyResponse(val customer: Customer, val ephemeralKey: String)