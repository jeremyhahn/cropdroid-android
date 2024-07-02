package com.jeremyhahn.cropdroid.ui.workflow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.config.WorkflowParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import java.io.IOException

class WorkflowViewModel(cropDroidAPI: CropDroidAPI) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI = cropDroidAPI
    val workflows = MutableLiveData<ArrayList<Workflow>>()

    fun getWorkflows() {
        cropDroidAPI.getWorkflowsView(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowViewModel.getWorkflows", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
//                    fragmentActivity.runOnUiThread {
//                        AppError(fragmentContext).apiAlert(apiResponse)
//                    }
                    Log.d("WorkflowViewModel", "error: ${apiResponse.error}")
                    return
                }
                if(apiResponse.payload != null) {
                    var responseBody = response.body().string()
                    Log.d("WorkflowViewModel.getWorkflows", "responseBody: " + responseBody)
                    if (response.code() != 200) {
                        return
                    }
                    workflows.postValue(WorkflowParser.parse(JSONArray(apiResponse.payload)))
                }
            }
        })
    }
}
