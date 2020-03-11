package com.jeremyhahn.cropdroid.utils

import com.jeremyhahn.cropdroid.model.Channel
import org.json.JSONArray
import org.json.JSONObject

class ChannelParser {

    companion object {
        fun Parse(json: String): ArrayList<Channel> {
            return Parse(JSONArray(json))
        }

        fun Parse(jsonChannels : JSONArray) : ArrayList<Channel> {
            var channels = ArrayList<Channel>(jsonChannels.length())
            for (i in 0..jsonChannels.length() - 1) {
                val jsonChannel = jsonChannels.getJSONObject(i)
                val id = jsonChannel.getInt("id")
                val name = jsonChannel.getString("name")
                val value = jsonChannel.getInt("value")
                channels.add(Channel(id, name, value))
            }
            return channels
        }

        fun Parse(jsonChannels : JSONObject) : ArrayList<Channel> {
            var channels = ArrayList<Channel>(jsonChannels.length())
            for(i in 0..jsonChannels.length()-1) {
                val v = jsonChannels.getInt(i.toString())
                channels.add(Channel(i, "", v))
            }
            return channels
        }
    }
}