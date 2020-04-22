package com.jeremyhahn.cropdroid.ui.microcontroller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.MetricDetailActivity
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import java.io.IOException

class MetricHistoryMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, controllerType: ControllerType) {

    init {
        menu.add(0, metric.id, 0, "History")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                cropDroidAPI.getMetricHistory(controllerType, metric.key, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.History", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {

                        var responseBody = response.body().string()

                        Log.d("onCreateContextMenu.History", "onResponse response: " + responseBody)

                        var jsonArray = JSONArray(responseBody)
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