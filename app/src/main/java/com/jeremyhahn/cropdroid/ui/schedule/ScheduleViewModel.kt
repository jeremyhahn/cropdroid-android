package com.jeremyhahn.cropdroid.ui.schedule

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Schedule
import com.jeremyhahn.cropdroid.config.ScheduleParser
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ScheduleViewModel(cropDroidAPI: CropDroidAPI, channelId: Long) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private val channelId: Long
    val schedules = MutableLiveData<ArrayList<Schedule>>()

    init {
        this.cropDroidAPI = cropDroidAPI
        this.channelId = channelId
    }

    fun getSchedule() {
        cropDroidAPI.getSchedule(channelId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ScheduleViewModel.getSchedule()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()
                Log.d("ScheduleViewModel.getSchedule()", "responseBody: " + responseBody)
                if (response.code() != 200) {
                    return
                }
                schedules.postValue(ScheduleParser.parse(responseBody))
            }
        })
    }

    fun add(schedule: Schedule) {
        val list = schedules.value
        list!!.add(schedule)
        schedules.postValue(list)
    }

/*
    fun update(schedule: Schedule) {
        val _schedules = ArrayList<Schedule>(schedules.value!!.size)
        for((i, s) in schedules.value!!.withIndex()) {
            _schedules[i] = if(s.id == schedule.id) schedule else s
        }
        schedules.postValue(_schedules)
    }
*/
}