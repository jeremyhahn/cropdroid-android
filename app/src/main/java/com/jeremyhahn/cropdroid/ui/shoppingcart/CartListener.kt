package com.jeremyhahn.cropdroid.ui.shoppingcart

interface CartListener {
    fun checkout()
    fun clear()
    fun editAddress()
    fun editDefaultPaymentMethod()
    fun saveDefaultPaymentMethod()
    fun cancelDefaultPaymentMethod()
}
