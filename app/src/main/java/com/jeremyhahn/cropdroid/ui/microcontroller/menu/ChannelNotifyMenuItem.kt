package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelNotifyMenuItem(context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Notify")
            .setCheckable(true)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                channel.notify = if(it.isChecked) false else true
                cropDroidAPI.setChannelConfig(channel, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.Notify", "onFailure response: " + e!!.message)
                        AppError(context).exception(e)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        Log.d("onCreateContextMenu.Notify", "onResponse response: " + response.body().string())
                    }
                })
                true
            })
            .setChecked(channel.notify)
    }
}