package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.MicroControllerRecyclerAdapter
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import kotlinx.android.synthetic.main.dialog_edit_number.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricSeValueMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter, controllerType: ControllerType) {

    init {
        menu.add(0, metric.id, 0, "Set Value")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_decimal, null)
                dialogView.editNumber.setText(metric.value.toString())

                val d = AlertDialog.Builder(context)
                d.setTitle(metric.name)
                d.setMessage(R.string.dialog_message_metric_value)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Duration", "onClick: " + it.itemId)
                    metric.value = dialogView.editNumber.text.toString().toDouble()
                    cropDroidAPI.setVirtualMetricValue(controllerType, metric, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.SetValue", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.SetValue", "onResponse response: " + response.body().string())
                            adapter.activity.runOnUiThread(Runnable() {
                                adapter.notifyDataSetChanged()
                            })
                        }
                    })
                }
                d.setNegativeButton("Cancel") { dialogInterface, i ->
                }
                d.create().show()
                true
            })
    }
}