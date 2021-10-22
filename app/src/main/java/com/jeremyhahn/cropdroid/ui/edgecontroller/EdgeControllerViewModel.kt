package com.jeremyhahn.cropdroid.ui.room

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection

class EdgeControllerViewModel(repository: EdgeDeviceRepository) : ViewModel() {

    private val repository: EdgeDeviceRepository
    val controllers = MutableLiveData<ArrayList<Connection>>()

    init {
        this.repository = repository
    }

    fun getMasterControllers() {
        var savedControllers = repository.allControllers
        for(controller in savedControllers) {
            Log.d("savedController", controller.toString())
        }
        controllers.postValue(savedControllers)
    }

}