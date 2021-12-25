package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class UserAccountsViewModelFactory(private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserAccountsViewModel(cropDroidAPI) as T
    }
}