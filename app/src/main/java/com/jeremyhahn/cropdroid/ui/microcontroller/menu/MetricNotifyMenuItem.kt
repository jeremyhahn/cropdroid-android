package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricNotifyMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu!!.add(0, metric.id.toInt(), 0, "Notify")
            .setCheckable(true)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                metric.notify = if(it.isChecked) false else true
                cropDroidAPI.setMetricConfig(metric, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.Notify", "onFailure response: " + e!!.message)
                        AppError(context).exception(e)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        Log.d("onCreateContextMenu.Notify", "onResponse response: " + response.body().string())
                    }
                })
                true
            })
            .setChecked(metric.notify)
    }
}