package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.Token
import org.json.JSONObject

class TokenParser {
    companion object {
        fun parse(json: String) : Token {
            Log.d("TokenParser", "json: " + json)
            val jsonToken = JSONObject(json)
            val token = jsonToken.getString("token")
            val error = jsonToken.getString("error")
            return Token(token, error)
        }
    }
}