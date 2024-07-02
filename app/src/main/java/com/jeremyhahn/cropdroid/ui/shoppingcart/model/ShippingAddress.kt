package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class ShippingAddress(
    var id: Long,
    var name: String,
    var phone: String,
    var address: Address
)
