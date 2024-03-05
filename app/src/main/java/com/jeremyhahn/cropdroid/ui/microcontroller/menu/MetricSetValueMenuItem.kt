package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricSetValueMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter, controllerType: String) {

    init {
        menu.add(0, metric.id.toInt(), 0, "Set Value")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(R.layout.dialog_edit_decimal, null)

                val editNumber = dialogView.findViewById(R.id.editNumber) as EditText
                editNumber.setText(metric.value.toString())

                val d = AlertDialog.Builder(context)
                d.setTitle(metric.name)
                d.setMessage(R.string.dialog_message_metric_value)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Duration", "onClick: " + it.itemId)
                    metric.value = editNumber.text.toString().toDouble()
                    cropDroidAPI.setMetricValue(controllerType, metric, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.SetValue", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.SetValue", "onResponse response: " + response.body().string())
                            adapter.activity.runOnUiThread(Runnable() {
                                for((i, recyclerModel) in  adapter.recyclerItems.withIndex()) {
                                    if(recyclerModel.metric!!.id.toInt() == it.itemId) {
                                        adapter.recyclerItems[i] = MicroControllerRecyclerModel(MicroControllerRecyclerModel.METRIC_TYPE, metric, null)
                                        adapter.notifyDataSetChanged()
                                        break
                                    }
                                }
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