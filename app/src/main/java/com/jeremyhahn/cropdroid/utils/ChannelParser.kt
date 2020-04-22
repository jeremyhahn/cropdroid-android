package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Channel
import org.json.JSONArray
import org.json.JSONObject

class ChannelParser {

    companion object {
        fun parse(json: String): ArrayList<Channel> {
            Log.d("[ChannelParser.parse] json: ", json)
            return parse(JSONArray(json))
        }

        fun parse(jsonChannels : JSONArray) : ArrayList<Channel> {
            Log.d("[ChannelParser.parse] jsonArray: ", jsonChannels.toString())
            var channels = ArrayList<Channel>(jsonChannels.length())
            for (i in 0..jsonChannels.length() - 1) {
                val jsonChannel = jsonChannels.getJSONObject(i)
                Log.d("ChannelParser.parse", jsonChannel.toString())
                channels.add(parse(jsonChannel))
            }
            return channels
        }

        fun parse(jsonChannel: JSONObject) : Channel {
            val id = jsonChannel.getInt("id")
            val controllerId = jsonChannel.getInt("controllerId")
            val channelId = jsonChannel.getInt("channelId")
            val name = jsonChannel.getString("name")
            val enable = jsonChannel.getBoolean("enable")
            val notify = jsonChannel.getBoolean("notify")
            val condition = jsonChannel.getString("condition")
            val duration = jsonChannel.getInt("duration")
            val debounce = jsonChannel.getInt("debounce")
            val backoff = jsonChannel.getInt("backoff")
            val algorithmId = jsonChannel.getInt("algorithmId")
            val value = jsonChannel.getInt("value")
            return Channel(id, controllerId, channelId, name, enable, notify, condition, duration, debounce, backoff, algorithmId, value)
        }
    }
}