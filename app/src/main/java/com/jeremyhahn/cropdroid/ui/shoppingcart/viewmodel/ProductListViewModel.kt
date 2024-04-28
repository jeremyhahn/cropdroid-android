package com.jeremyhahn.cropdroid.ui.shoppingcart.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.config.ProductParser
import com.jeremyhahn.cropdroid.ui.shoppingcart.model.Product
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import java.io.IOException
import kotlin.collections.ArrayList

class ProductListViewModel(var activity: Activity, var cropDroidAPI: CropDroidAPI) : ViewModel() {

    val products = MutableLiveData<ArrayList<Product>>()
    val error = MutableLiveData<String>()

    fun getProducts() {
        cropDroidAPI.getProducts(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ProductViewModel.getState()", "onFailure response: " + e!!.message)
                activity.runOnUiThread {
                    AppError(activity).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    activity.runOnUiThread {
                        AppError(activity).apiAlert(apiResponse)
                        error.postValue(apiResponse.error)
                    }
                    return
                }
                Log.d("ProductViewModel.getProducts()", "payload: " + apiResponse.payload)
                val jsonProducts = apiResponse.payload as JSONArray
                val parsedProducts = ProductParser.parse(jsonProducts)
                products.postValue(parsedProducts)
            }
        })
    }
}