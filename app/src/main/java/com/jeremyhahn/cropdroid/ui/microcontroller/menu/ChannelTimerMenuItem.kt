package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import kotlinx.android.synthetic.main.dialog_edit_duration.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelTimerMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id, 0, "Timer")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_duration, null)

                if(channel.duration >= Constants.SECONDS_IN_YEAR) {
                    dialogView.durationSpinner.setSelection(6)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_YEAR).toString())
                } else if(channel.duration >= Constants.SECONDS_IN_MONTH) {
                    dialogView.durationSpinner.setSelection(5)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_MONTH).toString())
                } else if(channel.duration >= Constants.SECONDS_IN_WEEK) {
                    dialogView.durationSpinner.setSelection(4)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_WEEK).toString())
                } else if(channel.duration >= Constants.SECONDS_IN_DAY) {
                    dialogView.durationSpinner.setSelection(3)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_DAY).toString())
                } else if(channel.duration >= Constants.SECONDS_IN_HOUR) {
                    dialogView.durationSpinner.setSelection(2)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_HOUR).toString())
                } else if(channel.duration >= Constants.SECONDS_IN_MINUTE) {
                    dialogView.durationSpinner.setSelection(1)
                    dialogView.editDuration.setText((channel.duration / Constants.SECONDS_IN_MINUTE).toString())
                } else {
                    dialogView.durationSpinner.setSelection(0)
                    dialogView.editDuration.setText(channel.duration.toString())
                }

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_duration)
                d.setMessage(R.string.dialog_message_duration)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->

                    Log.d("Duration", "onClick: " + it.itemId + ", time_type=" + dialogView.durationSpinner.selectedItem)

                    val duration = dialogView.editDuration.text.toString().toInt()
                    when(dialogView.durationSpinner.selectedItem) {
                        "Seconds" -> //R.array.time_entries[0] ->
                            channel.duration = duration
                        "Minutes" -> //R.array.time_entries[1] ->
                            channel.duration = duration * Constants.SECONDS_IN_MINUTE
                        "Hours" -> //R.array.time_entries[2] ->
                            channel.duration = duration * Constants.SECONDS_IN_HOUR
                        "Days" ->
                            channel.duration = duration * Constants.SECONDS_IN_DAY
                        "Weeks" ->
                            channel.duration = duration * Constants.SECONDS_IN_WEEK
                        "Months" ->
                            channel.duration = duration * Constants.SECONDS_IN_MONTH
                        "Years" ->
                            channel.duration = duration * Constants.SECONDS_IN_YEAR
                        else ->
                            Log.d("onCreateContextMenu.Duration", "Unsupported spinner item: " + dialogView.durationSpinner.selectedItem)
                    }

                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Duration", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
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