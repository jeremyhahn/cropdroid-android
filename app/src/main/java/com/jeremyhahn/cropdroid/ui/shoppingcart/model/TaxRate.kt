package com.jeremyhahn.cropdroid.ui.shoppingcart.model

data class TaxRate(
    var id: String,
    var displayName: String,
    var description: String,
    var state: String,
    var country: String,
    var inclusive: Boolean,
    var jurisdiction: String,
    var percentage: Double
)
