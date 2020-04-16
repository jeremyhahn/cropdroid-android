package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import kotlinx.android.synthetic.main.dialog_edit_text.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MetricRenameMenuItem(context: Context, menu: ContextMenu, metric: Metric, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu.add(0, metric.id, 0, "Rename")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                dialogView.editText.setText(metric.name)

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_rename)
                d.setMessage(R.string.dialog_message_rename)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Rename", "onClick: " + it.itemId)
                    metric.name = dialogView.editText.text.toString()
                    cropDroidAPI.setMetricConfig(metric, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Rename", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Rename", "onResponse response: " + response.body().string())
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