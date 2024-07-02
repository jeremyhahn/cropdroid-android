package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricAlarmMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu.add(0, metric.id.toInt(), 0, "Alarm")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_alarm, null)
                val alarmLow = dialogView.findViewById(R.id.alarmLow) as EditText
                val alarmHigh = dialogView.findViewById(R.id.alarmHigh) as EditText

                alarmLow.setText(metric.alarmLow.toString())
                alarmHigh.setText(metric.alarmHigh.toString())

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_alarm)
                d.setMessage(R.string.dialog_message_alarm)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Alarm", "onClick: " + it.itemId)
                    metric.alarmLow = alarmLow.text.toString().toDouble()
                    metric.alarmHigh = alarmHigh.text.toString().toDouble()
                    cropDroidAPI.setMetricConfig(metric, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Alarm", "onFailure response: " + e!!.message)
                            AppError(context).exception(e)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Alarm", "onResponse response: " + response.body().string())

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