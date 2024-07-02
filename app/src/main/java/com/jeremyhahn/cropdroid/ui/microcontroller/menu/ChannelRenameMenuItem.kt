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
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelRenameMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Rename")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                val editText = dialogView.findViewById(R.id.editText) as EditText
                editText.setText(channel.name)

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_rename)
                d.setMessage(R.string.dialog_message_rename)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Rename", "onClick: " + it.itemId)
                    channel.name = editText.text.toString()
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Rename", "onFailure response: " + e!!.message)
                            AppError(context).exception(e)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Rename", "onResponse response: " + response.body().string())
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