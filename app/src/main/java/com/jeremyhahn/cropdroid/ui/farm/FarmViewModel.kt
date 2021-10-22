package com.jeremyhahn.cropdroid.ui.farm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.config.FarmParser
import com.jeremyhahn.cropdroid.config.ScheduleParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Farm
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class FarmViewModel(cropDroidAPI: CropDroidAPI, orgId: Long) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private val orgId: Long
    private val brief = true // Shallow population of Farm object (org id, farm id, farm name)
    val farms = MutableLiveData<ArrayList<Farm>>()

    init {
        this.cropDroidAPI = cropDroidAPI
        this.orgId = orgId
    }

    fun getFarms() {
        cropDroidAPI.getFarms(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("FarmViewModel.getFarms()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()
                Log.d("FarmViewModel.getFarms()", "responseBody: " + responseBody)
                if (response.code() != 200) {
                    return
                }
                farms.postValue(FarmParser.parse(responseBody, orgId, brief))
            }
        })
    }


}