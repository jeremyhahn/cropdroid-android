package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Channel
import org.json.JSONArray

class ChannelParser {

    companion object {
        fun parse(json: String): ArrayList<Channel> {
            return parse(JSONArray(json))
        }

        fun parse(jsonChannels : JSONArray) : ArrayList<Channel> {
            var channels = ArrayList<Channel>(jsonChannels.length())
            for (i in 0..jsonChannels.length() - 1) {
                val jsonChannel = jsonChannels.getJSONObject(i)

                Log.d("ChannelParser.parse", jsonChannel.toString())

                val id = jsonChannel.getInt("id")
                val channelId = jsonChannel.getInt("channelId")
                val name = jsonChannel.getString("name")
                val enable = jsonChannel.getBoolean("enable")
                val notify = jsonChannel.getBoolean("notify")
                val condition = jsonChannel.getString("condition")
                val schedule = jsonChannel.getString("schedule")
                val duration = jsonChannel.getInt("duration")
                val debounce = jsonChannel.getInt("debounce")
                val backoff = jsonChannel.getInt("backoff")
                val value = jsonChannel.getInt("value")
                channels.add(Channel(id, channelId, name, enable, notify, condition, schedule, duration, debounce, backoff, value))
            }
            return channels
        }

/*
        fun parse(jsonChannels : JSONObject) : ArrayList<Channel> {
            var channels = ArrayList<Channel>(jsonChannels.length())
            for(i in 0..jsonChannels.length()-1) {
                val v = jsonChannels.getInt(i.toString())
                channels.add(Channel(i, "", true, v))
            }
            return channels
        }
        */
    }
}