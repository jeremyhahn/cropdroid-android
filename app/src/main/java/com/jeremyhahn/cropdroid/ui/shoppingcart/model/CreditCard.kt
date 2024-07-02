package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class CreditCard(
    var id: Long,
    var brand: String,
    var country: String,
    var expMonth: String,
    var expYear: String,
    var last4: String,
    var threeDSecureStorage: Boolean,
    var checks: CreditCardChecks,
)
