package com.jeremyhahn.cropdroid.ui.microcontroller

import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricEnableMenuItem(menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu.add(0, metric.id, 0, "Enable")
            .setCheckable(true)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                metric.enable = if (it.isChecked) false else true
                cropDroidAPI.setMetricConfig(metric, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.Enable", "onFailure response: " + e!!.message)
                        return
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        Log.d(
                            "onCreateContextMenu.Enable",
                            "onResponse response: " + response.body().string()
                        )
                        adapter.activity.runOnUiThread(Runnable() {
                            adapter.notifyDataSetChanged()
                        })
                    }
                })
                true
            })
            .setChecked(metric.enable)
    }
}