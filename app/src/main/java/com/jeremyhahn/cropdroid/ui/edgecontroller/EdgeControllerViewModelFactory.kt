package com.jeremyhahn.cropdroid.ui.edgecontroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.ui.room.EdgeControllerViewModel

class EdgeControllerViewModelFactory(private val repository: MasterControllerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EdgeControllerViewModel(repository) as T
    }
}