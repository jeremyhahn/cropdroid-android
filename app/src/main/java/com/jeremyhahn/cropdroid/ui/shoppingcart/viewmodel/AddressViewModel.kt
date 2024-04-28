package com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Address

class AddressViewModel(address: Address) : ViewModel() {

    var line1 = MutableLiveData<String>()
    var line2 = MutableLiveData<String>()
    var city = MutableLiveData<String>()
    var state = MutableLiveData<String>()
    var postalCode = MutableLiveData<String>()
    var country = MutableLiveData<String>()

    init {
        setAddress(address)
    }

    fun setAddress(address: Address) {
        line1.postValue(address.line1)
        line2.postValue(address.line2)
        city.postValue(address.city)
        state.postValue(address.state)
        postalCode.postValue(address.postalCode)
        country.postValue(address.country)
    }
}