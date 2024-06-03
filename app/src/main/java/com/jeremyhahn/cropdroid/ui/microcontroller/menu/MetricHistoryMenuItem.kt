package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.ui.microcontroller.MetricDetailActivity
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.EventLog
import com.jeremyhahn.cropdroid.model.EventsPage
import com.jeremyhahn.cropdroid.model.Metric
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MetricHistoryMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, controllerType: String) {

    init {
        menu.add(0, metric.id.toInt(), 0, "History")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                cropDroidAPI.getMetricHistory(controllerType, metric.key, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.History", "onFailure response: " + e!!.message)
                        AppError(context).exception(e)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val apiResponse = APIResponseParser.parse(response)
                        if (apiResponse.code != 200) {
                            AppError(context).apiAlert(apiResponse)
                            return
                        }
                        if (!apiResponse.success) {
                            AppError(context).apiAlert(apiResponse)
                            return
                        }
                        val jsonArray = apiResponse.payload as JSONArray
                        var values = DoubleArray(jsonArray.length())
                        for(i in 0..jsonArray.length()-1) {
                            values[i] = jsonArray.getDouble(i)
                        }

                        Log.d("onCreateContextMenu.History", "values: " + values)

                        var intent = Intent(context, MetricDetailActivity::class.java)
                        intent.putExtra("metric", metric.name)
                        intent.putExtra("values", values)
                        context.startActivity(intent)
                    }
                })
                true
            })
    }
}