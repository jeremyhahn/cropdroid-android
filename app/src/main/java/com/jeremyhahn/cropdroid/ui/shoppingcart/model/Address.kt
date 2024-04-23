package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class Address(
    var id: Long,
    var line1: String,
    var line2: String,
    var city: String,
    var state: String,
    var postalCode: String,
    var country: String
)
