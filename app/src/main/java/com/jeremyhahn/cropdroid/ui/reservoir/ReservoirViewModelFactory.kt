package com.jeremyhahn.cropdroid.ui.reservoir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class ReservoirViewModelFactory(private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReservoirViewModel(cropDroidAPI) as T
    }
}