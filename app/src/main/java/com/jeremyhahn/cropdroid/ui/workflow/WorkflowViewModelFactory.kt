package com.jeremyhahn.cropdroid.ui.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeremyhahn.cropdroid.data.CropDroidAPI

class WorkflowViewModelFactory(private val cropDroidAPI: CropDroidAPI) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkflowViewModel(cropDroidAPI) as T
    }
}