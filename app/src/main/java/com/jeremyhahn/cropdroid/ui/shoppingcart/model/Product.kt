package com.jeremyhahn.cropdroid.ui.shoppingcart.model

import androidx.databinding.BaseObservable
import java.math.BigDecimal

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Long,
    var quantity: Int) : BaseObservable() {
}