package com.jeremyhahn.cropdroid.ui.organization

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.config.OrganizationParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Organization
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class OrganizationViewModel(cropDroidAPI: CropDroidAPI) : ViewModel() {

    private val cropDroidAPI: CropDroidAPI
    private val brief = true // Shallow population of Organization/Farm object (org id, farm id, farm name)
    val organizations = MutableLiveData<ArrayList<Organization>>()

    init {
        this.cropDroidAPI = cropDroidAPI
    }

    fun getOrganizations() {
        cropDroidAPI.getOrganizations(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("UserAccountsViewModel.getFarms()", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var responseBody = response.body().string()
                Log.d("UserAccountsViewModel.getFarms()", "responseBody: " + responseBody)
                if (response.code() != 200) {
                    return
                }
                organizations.postValue(OrganizationParser.parse(responseBody, brief))
            }
        })
    }


}