package com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.ui.shoppingcart.CartListener
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.ShippingAddress

class CartViewModel(private var taxRate: Double = 0.0) : ViewModel() {

    private val TAG = "CartViewModel"
    private lateinit var cartListener: CartListener

    val items: MutableLiveData<HashMap<String, Product>> = MutableLiveData<HashMap<String, Product>>()
    val size = MutableLiveData<Int>()
    var tax = MutableLiveData<Double>()
    var subtotal = MutableLiveData<Double>()
    var total = MutableLiveData<Double>()

    var shippingAddress = MutableLiveData<ShippingAddressViewModel>()

    init {
        items.value = HashMap()
        size.apply { value = 0 }
        tax.apply { value = 0.0 }
        subtotal.apply { value = 0.0 }
        total.apply { value = 0.0 }
    }

    fun setShippingAddress(shippingAddress: ShippingAddress) {
        this.shippingAddress.postValue(ShippingAddressViewModel(shippingAddress))
    }

    fun editAddress() {
        cartListener.editAddress()
    }

    fun registerListener(listener: CartListener) {
        cartListener = listener
    }

    fun setTaxRate(rate: Double) {
        taxRate = rate
        updateCounters()
    }

    fun set(product: Product) {
        items.value!![product.id] = product
        updateCounters()
    }

    fun remove(product: Product) {
        if(product.quantity == 0) {
            items.value!!.remove(product.id)
        } else {
            items.value!![product.id] = product
        }
        updateCounters()
    }

    fun getProducts(): ArrayList<Product> {
        return ArrayList(items.value!!.values)
    }

    fun getProduct(id: String): Product? {
        return items.value!![id]
    }

    fun clear() {
        items.value!!.clear()
        updateCounters()
        cartListener.clear()
    }

    private fun updateCounters() {
        val _subtotal = calculateSubotal() / 100
        val _tax = _subtotal * (taxRate / 100)
        val _total = _subtotal + _tax
        val _size = calculateSize()
        subtotal.postValue(_subtotal)
        tax.postValue(_tax)
        total.postValue(_total)
        size.postValue(_size)
    }

    fun calculateSize(): Int {
        var s = 0
        for (product in items.value!!.values) {
            s = (s + product.quantity)
        }
        return s
    }

    private fun calculateSubotal(): Double {
        var t = 0.0
        for (product in items.value!!.values) {
            t = (t + (product.price * product.quantity))
        }
        return t
    }
}