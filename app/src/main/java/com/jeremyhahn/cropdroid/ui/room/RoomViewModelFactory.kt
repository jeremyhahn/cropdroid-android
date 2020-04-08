package com.jeremyhahn.cropdroid.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class RoomViewModelFactory(private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RoomViewModel(cropDroidAPI) as T
    }
}