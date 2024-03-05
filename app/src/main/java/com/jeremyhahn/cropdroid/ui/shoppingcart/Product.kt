package com.jeremyhahn.cropdroid.ui.shoppingcart

import androidx.databinding.BaseObservable

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    var quantity: Int) : BaseObservable() {
}