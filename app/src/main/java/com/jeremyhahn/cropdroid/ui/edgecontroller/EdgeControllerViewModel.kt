package com.jeremyhahn.cropdroid.ui.room

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.ClientConfig

class EdgeControllerViewModel(repository: MasterControllerRepository) : ViewModel() {

    private val repository: MasterControllerRepository
    val controllers = MutableLiveData<ArrayList<ClientConfig>>()

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