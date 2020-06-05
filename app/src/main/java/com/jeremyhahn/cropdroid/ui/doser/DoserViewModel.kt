package com.jeremyhahn.cropdroid.ui.doser

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.config.ChannelParser
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class DoserViewModel(cropDroidAPI: CropDroidAPI) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private var refreshTimer: Timer
    val channels = MutableLiveData<ArrayList<Channel>>()
    val models = MutableLiveData<ArrayList<MicroControllerRecyclerModel>>()

    init {
        this.cropDroidAPI = cropDroidAPI
        refreshTimer = Timer()
        refreshTimer.scheduleAtFixedRate(timerTask {
            getDoserStatus()
        }, 0, Constants.MICROCONTROLLER_REFRESH)
    }

    fun getDoserStatus() {
        cropDroidAPI.getState(Constants.CONFIG_DOSER_KEY, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("DoserViewModel.getDoserStatus()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()

                Log.d("DoserViewModel.getDoserStatus()", "responseBody: " + responseBody)

                if (response.code() != 200) {
                    return
                }

                val json = JSONObject(responseBody)

                val jsonChannels = json.getJSONArray("channels")
                var _channels = ChannelParser.parse(jsonChannels)
                channels.postValue(_channels)

                val _models = ArrayList<MicroControllerRecyclerModel>(_channels.size)
                for(channel in _channels) {
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel))
                }
                models.postValue(_models)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        refreshTimer.cancel()
        refreshTimer.purge()
    }
}