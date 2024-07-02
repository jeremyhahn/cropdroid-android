package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class PaymentMethod(
    var id: Long,
    var type: String,
    var card: CreditCard
)
