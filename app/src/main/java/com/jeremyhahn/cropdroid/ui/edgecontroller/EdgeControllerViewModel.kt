package com.jeremyhahn.cropdroid.ui.room

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.utils.ChannelParser
import com.jeremyhahn.cropdroid.utils.MetricParser
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class EdgeControllerViewModel(repository: MasterControllerRepository) : ViewModel() {

    private val repository: MasterControllerRepository
    val controllers = MutableLiveData<ArrayList<MasterController>>()

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