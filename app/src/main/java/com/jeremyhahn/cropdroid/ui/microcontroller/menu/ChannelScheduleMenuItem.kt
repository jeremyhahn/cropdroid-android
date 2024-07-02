package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.content.Context
import android.content.Intent
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.ui.schedule.ScheduleListActivity

class ChannelScheduleMenuItem(context: Context, menu: ContextMenu, channel: Channel) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Schedule")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                //activity.startActivity(Intent(activity, WeekViewActivity::class.java))

                var intent = Intent(context, ScheduleListActivity::class.java)
                intent.putExtra("channel_id", channel.id)
                intent.putExtra("channel_name", channel.name)
                intent.putExtra("channel_duration", channel.duration)
                context.startActivity(intent)

                /*
                var intent = Intent(v!!.context, ScheduleActivity::class.java)
                intent.putExtra("channel_id", channel.id)
                v.context.startActivity(intent)
                */

                true
            })
    }
}