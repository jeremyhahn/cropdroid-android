package com.jeremyhahn.cropdroid.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class ScheduleViewModelFactory(private val cropDroidAPI: CropDroidAPI, private val channelId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ScheduleViewModel(cropDroidAPI, channelId) as T
    }
}