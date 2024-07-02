package com.jeremyhahn.cropdroid.ui.shoppingcart.rest

data class SetDefaultPaymentMethodRequest(
    val customerId: Long,
    val processorId: String,
    val paymentMethodId: String
)
