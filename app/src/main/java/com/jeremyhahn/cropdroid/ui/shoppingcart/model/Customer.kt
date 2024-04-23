package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class Customer(
    var id: Long,
    var processorId: String,
    var description: String,
    var name: String,
    var email: String,
    var phone: String,
    var address: Address?,
    var shipping: ShippingAddress?
)
