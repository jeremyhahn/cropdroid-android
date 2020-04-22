package com.jeremyhahn.cropdroid.ui.condition

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.utils.ConditionParser
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ConditionViewModel(cropDroidAPI: CropDroidAPI, channelId: Int) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private val channelId: Int
    val conditions = MutableLiveData<ArrayList<Condition>>()

    init {
        this.cropDroidAPI = cropDroidAPI
        this.channelId = channelId
    }

    fun getConditions() {
        cropDroidAPI.getConditions(channelId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionViewModel.getConditions", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()
                Log.d("ConditionViewModel.getConditions", "responseBody: " + responseBody)
                if (response.code() != 200) {
                    return
                }
                conditions.postValue(ConditionParser.parse(responseBody))
            }
        })
    }
}
