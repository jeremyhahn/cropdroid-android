package com.jeremyhahn.cropdroid.ui.microcontroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class ControllerViewModelFactory(private val cropDroidAPI: CropDroidAPI, private val controllerType: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ControllerViewModel(cropDroidAPI, controllerType) as T
    }
}