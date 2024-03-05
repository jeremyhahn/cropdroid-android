package com.jeremyhahn.cropdroid.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class OrganizationViewModelFactory(private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OrganizationViewModel(cropDroidAPI) as T
    }
}