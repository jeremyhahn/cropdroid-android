package com.jeremyhahn.cropdroid.ui.condition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class ConditionViewModelFactory(private val cropDroidAPI: CropDroidAPI, private val channelId: Long) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConditionViewModel(cropDroidAPI, channelId) as T
    }
}