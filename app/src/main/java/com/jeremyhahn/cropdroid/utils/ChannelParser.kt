package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Channel
import org.json.JSONArray
import org.json.JSONObject

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
                val name = jsonChannel.getString("name")
                val value = jsonChannel.getInt("value")
                channels.add(Channel(id, name, value))
            }
            return channels
        }

        fun parse(jsonChannels : JSONObject) : ArrayList<Channel> {
            var channels = ArrayList<Channel>(jsonChannels.length())
            for(i in 0..jsonChannels.length()-1) {
                val v = jsonChannels.getInt(i.toString())
                channels.add(Channel(i, "", v))
            }
            return channels
        }
    }
}