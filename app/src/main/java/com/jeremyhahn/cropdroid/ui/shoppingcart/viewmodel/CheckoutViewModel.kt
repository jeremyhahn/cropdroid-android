package com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CheckoutViewModel() : ViewModel() {

    var creditCardLast4 = MutableLiveData<String>()

    fun setCreditCardLast4(last4: String) {
        this.creditCardLast4.postValue(last4)
    }
}