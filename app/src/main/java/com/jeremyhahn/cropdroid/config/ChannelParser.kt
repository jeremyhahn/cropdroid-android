package com.jeremyhahn.cropdroid.config

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
                //Log.d("ChannelParser.parse", jsonChannel.toString())
                channels.add(parse(jsonChannel))
            }
            return channels
        }

        fun parse(jsonChannel: JSONObject) : Channel {
            val id = jsonChannel.getLong("id")
            val controllerId =  if(jsonChannel.isNull("device_id")) jsonChannel.getLong("deviceId") else jsonChannel.getLong("device_id")
            val channelId = if(jsonChannel.isNull("channel_id")) jsonChannel.getLong("channelId") else jsonChannel.getLong("channel_id")
            val name = jsonChannel.getString("name")
            val enable = jsonChannel.getBoolean("enable")
            val notify = jsonChannel.getBoolean("notify")
            val duration = jsonChannel.getInt("duration")
            val debounce = jsonChannel.getInt("debounce")
            val backoff = jsonChannel.getInt("backoff")
            val algorithmId = if(jsonChannel.isNull("algorithm_id")) jsonChannel.getLong("algorithmId") else jsonChannel.getLong("algorithm_id")
            val value = if(!jsonChannel.isNull("value")) jsonChannel.getInt("value") else 0
            return Channel(id, controllerId, channelId, name, enable, notify, duration, debounce, backoff, algorithmId, value)
        }
    }
}