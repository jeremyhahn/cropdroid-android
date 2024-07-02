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
import com.jeremyhahn.cropdroid.model.Channel
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelBackoffMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Backoff")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)


                val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)

                val editNumber = dialogView.findViewById(R.id.editNumber) as EditText
                editNumber.setText(channel.backoff.toString())

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_backoff)
                d.setMessage(R.string.dialog_message_backoff)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Backoff", "onClick: " + it.itemId)
                    channel.backoff = editNumber.text.toString().toInt()
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Backoff", "onFailure response: " + e!!.message)
                            AppError(context).exception(e)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Backoff", "onResponse response: " + response.body().string())
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