package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.utils.DurationUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ChannelTimerMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Timer")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(R.layout.dialog_edit_duration, null)

                val editDuration = dialogView.findViewById(R.id.editDuration) as EditText
                val durationSpinner = dialogView.findViewById(R.id.durationSpinner) as Spinner

                DurationUtil.setDuration(channel.duration, editDuration, durationSpinner)

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_duration)
                d.setMessage(R.string.dialog_message_duration)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->

                    Log.d("Duration", "onClick: " + it.itemId + ", time_type=" + durationSpinner.selectedItem)

                    channel.duration = DurationUtil.parseDuration(
                        editDuration.text.toString().toInt(),
                        durationSpinner.selectedItem.toString())

                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Duration", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: Response) {
                            Log.d("onCreateContextMenu.Duration", "onResponse response: " + response.body().string())
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