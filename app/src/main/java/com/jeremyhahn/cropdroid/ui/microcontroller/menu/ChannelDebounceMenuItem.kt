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
import com.jeremyhahn.cropdroid.model.Channel
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelDebounceMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Debounce")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)

                val editNumber = dialogView.findViewById(R.id.editNumber) as EditText
                editNumber.setText(channel.debounce.toString())
                editNumber.setHint(R.string.minutes)

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_debounce)
                d.setMessage(R.string.dialog_message_debounce)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Debounce", "onClick: " + it.itemId)
                    channel.debounce = editNumber.text.toString().toInt()
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Debounce", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Debounce", "onResponse response: " + response.body().string())
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