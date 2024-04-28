package com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.ShippingAddress

class ShippingAddressViewModel(shippingAddress: ShippingAddress) : ViewModel() {

    val name = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val address = MutableLiveData<AddressViewModel>()

    init {
        setShippingAddress(shippingAddress)
    }

    fun setShippingAddress(shippingAddress: ShippingAddress) {
        name.postValue(shippingAddress.name)
        phone.postValue(shippingAddress.phone)
        address.postValue(AddressViewModel(shippingAddress.address))
    }
}
