package com.jeremyhahn.cropdroid.ui.microcontroller

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.config.ChannelParser
import com.jeremyhahn.cropdroid.config.ConfigObserver
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.config.MetricParser
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import kotlin.collections.ArrayList

class ControllerViewModel(cropDroidAPI: CropDroidAPI, controllerType: String) : ViewModel(), ConfigObserver {

    private val cropDroidAPI: CropDroidAPI
    private val controllerType: String
    //private var refreshTimer: Timer
    var metrics = ArrayList<Metric>()
    var channels = ArrayList<Channel>()
    val models = MutableLiveData<ArrayList<MicroControllerRecyclerModel>>()
    val error = MutableLiveData<String>()

    init {
        this.cropDroidAPI = cropDroidAPI
        this.controllerType = controllerType
        /*
        refreshTimer = Timer()
        refreshTimer.scheduleAtFixedRate(timerTask {
            getState()
        }, 0, Constants.MICROCONTROLLER_REFRESH)
         */
    }

    fun getState() {
        cropDroidAPI.getState(controllerType, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ProductViewModel.getState()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (apiResponse.code != 200) {
                    error.postValue(apiResponse.error)
                    Log.d("ControllerViewModel", "error: ${apiResponse.error}")
                    return
                }
                if (!apiResponse.success) {
                    error.postValue(apiResponse.error)
                    Log.d("ControllerViewModel", "error: ${apiResponse.error}")
                    return
                }

                val json = apiResponse.payload as JSONObject
                val jsonMetrics = json.getJSONArray("metrics")
                metrics = MetricParser.parse(jsonMetrics)

                val jsonChannels = json.getJSONArray("channels")
                channels = ChannelParser.parse(jsonChannels)

                val _models = ArrayList<MicroControllerRecyclerModel>(metrics.size + channels.size)
                for(metric in metrics) {
                    val metric = Metric(metric.id, metric.controllerId, metric.datatype, metric.key, metric.name, metric.enable, metric.notify, metric.unit, metric.alarmLow, metric.alarmHigh, metric.value)
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric,null))
                }
                for(channel in channels) {
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel))
                }
                models.postValue(_models)
            }
        })
    }

    override fun setConfig(controller: Controller) {
        Log.d("ProductViewModel.updateConfig", controller.toString())
        // Server doesn't send values with config updates; use current values
        for(metric in metrics) {
            for(newMetric in controller.metrics) {
                if(metric.id == newMetric.id) {
                    newMetric.value = metric.value
                }
            }
        }
        for(channel in channels) {
            for(newChannel in controller.channels) {
                if(channel.id == newChannel.id) {
                    newChannel.value = channel.value
                }
            }
        }
        metrics = controller.metrics
        metrics.sortBy { metric -> metric.name }
        channels = controller.channels
        channels.sortBy { channel -> channel.name }
        val _models = ArrayList<MicroControllerRecyclerModel>(controller.metrics.size + controller.channels.size)
        for(metric in controller.metrics) {
            val metric = Metric(metric.id, metric.controllerId, metric.datatype, metric.key, metric.name, metric.enable, metric.notify, metric.unit, metric.alarmLow, metric.alarmHigh, metric.value)
            _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric,null))
        }
        for(channel in controller.channels) {
            _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel))
        }
        models.postValue(_models)
    }

    override fun setState(state: ControllerState)  {
        val _models = ArrayList<MicroControllerRecyclerModel>(state.metrics.size + state.channels.size)
        for((k, v) in state.metrics) {
            for(metric in metrics) {
                if(metric.key == k) {
                    metric.value = v
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric,null))
                    break
                }
            }
        }
        for(i in state.channels) {
            for(channel in channels) {
                if(channel.boardId == i.toLong()) {
                    channel.value = i
                    _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel)
                    )
                    break
                }
            }
        }
        models.postValue(_models)
    }

    override fun setStateDelta(delta: ControllerStateDelta) {
        var size = 0
        if(models.value != null && models.value!!.size > 0) {
            size = models.value!!.size
        }
        val _models = ArrayList<MicroControllerRecyclerModel>(metrics.size + channels.size)
        for((i, metric) in metrics.withIndex()) {
            for((k, v) in delta.metrics) {
                if (metric.key == k) {
                    metric.value = v
                    break
                }
            }
            _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric, null))
        }
        for(channel in channels) {
            for((k, v)  in delta.channels) {
                if(channel.boardId == k.toLong()) {
                    channel.value = v
                    break
                }
            }
            _models.add(MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel))
        }
        models.postValue(_models)
    }
}