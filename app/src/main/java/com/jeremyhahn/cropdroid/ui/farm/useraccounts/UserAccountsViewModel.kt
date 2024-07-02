package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.config.UserParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.APIResponse
import com.jeremyhahn.cropdroid.model.UserConfig
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import java.io.IOException

class UserAccountsViewModel(cropDroidAPI: CropDroidAPI) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI = cropDroidAPI
    val users = MutableLiveData<ArrayList<UserConfig>>()
    val error = MutableLiveData<APIResponse>()
    val exception = MutableLiveData<IOException>()

    fun getUsers() {
        cropDroidAPI.getFarmUsers(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                exception.postValue(e)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if(!apiResponse.success) {
                    error.postValue(apiResponse)
                    return
                }
                val payload = apiResponse.payload.toString()
                val jsonArray = JSONArray(payload)
                users.postValue(UserParser.parse(jsonArray))
            }
        })
    }
}