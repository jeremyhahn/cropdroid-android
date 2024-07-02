package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel.ProductListViewModel

class ProductViewModelFactory(private val activity: Activity, private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductListViewModel(activity, cropDroidAPI) as T
    }
}