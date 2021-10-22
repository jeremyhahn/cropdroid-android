package com.jeremyhahn.cropdroid.ui.farm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.ui.room.EdgeControllerViewModel

class FarmViewModelFactory(private val cropDroidAPI: CropDroidAPI, private val orgId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FarmViewModel(cropDroidAPI, orgId) as T
    }
}