package com.jeremyhahn.cropdroid.ui.shoppingcart

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product

class CartViewModel() : ViewModel() {

    private val TAG = "Cart"
    private lateinit var cartListener: CartListener

    val items: MutableLiveData<HashMap<String, Product>> = MutableLiveData<HashMap<String, Product>>()
    val size = MutableLiveData<Int>()
    var total = MutableLiveData<Double>()

    init {
        items.value = HashMap()
        size.apply { value = 0 }
        total.apply { value = 0.0 }
    }

    fun registerListener(listener: CartListener) {
        cartListener = listener
    }

    fun checkout() {
        cartListener.checkout()
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
        size.postValue(calculateSize())
        total.postValue(calculateTotal() / 100)
    }

    fun calculateSize(): Int {
        var s = 0
        for (product in items.value!!.values) {
            s = (s + product.quantity)
        }
        return s
    }

    private fun calculateTotal(): Double {
        var t = 0.0
        for (product in items.value!!.values) {
            t = (t + (product.price * product.quantity))
        }
        return t
    }

}