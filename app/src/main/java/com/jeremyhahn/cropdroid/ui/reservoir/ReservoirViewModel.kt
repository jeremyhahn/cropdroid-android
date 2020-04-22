package com.jeremyhahn.cropdroid.ui.reservoir

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
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

class ReservoirViewModel(cropDroidAPI: CropDroidAPI) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private var refreshTimer: Timer
    val metrics = MutableLiveData<ArrayList<Metric>>()
    val channels = MutableLiveData<ArrayList<Channel>>()
    val models = MutableLiveData<ArrayList<MicroControllerRecyclerModel>>()

    init {
        this.cropDroidAPI = cropDroidAPI
        refreshTimer = Timer()
        refreshTimer.scheduleAtFixedRate(timerTask {
            getReservoirStatus()
        }, 0, Constants.MICROCONTROLLER_REFRESH)
    }

    fun getReservoirStatus() {
        cropDroidAPI.getState(ControllerType.Reservoir, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ReservoirViewModel.getReservoirStatus()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()

                Log.d("ReservoirViewModel.getReservoirStatus()", "responseBody: " + responseBody)

                if (response.code() != 200) {
                    return
                }

                val json = JSONObject(responseBody)
                val jsonMetrics = json.getJSONArray("metrics")
                val _metrics = MetricParser.parse(jsonMetrics)
                metrics.postValue(_metrics)

                val jsonChannels = json.getJSONArray("channels")
                var _channels = ChannelParser.parse(jsonChannels)
                channels.postValue(_channels)

                val _models = ArrayList<MicroControllerRecyclerModel>(_metrics.size + _channels.size)
                for(metric in _metrics) {
                    val metric = Metric(metric.id, metric.key, metric.name, metric.enable, metric.notify, metric.unit, metric.alarmLow, metric.alarmHigh, metric.value)
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric,null))
                }
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