package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelNotifyMenuItem(menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter) {

    init {
        menu!!.add(0, channel.id, 0, "Notify")
            .setCheckable(true)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                channel.notify = if(it.isChecked) false else true
                cropDroidAPI.setChannelConfig(channel, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.Notify", "onFailure response: " + e!!.message)
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