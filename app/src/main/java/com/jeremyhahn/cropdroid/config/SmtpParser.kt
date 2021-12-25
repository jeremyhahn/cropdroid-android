package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.SmtpConfig
import org.json.JSONObject

class SmtpParser {

    companion object {

        fun parse(jsonSmtp: JSONObject): SmtpConfig {
            Log.d("SmtpParser", jsonSmtp.toString())
            return SmtpConfig(
                jsonSmtp.getString("enable"),
                jsonSmtp.getString("host"),
                jsonSmtp.getString("port"),
                jsonSmtp.getString("username"),
                jsonSmtp.getString("password"),
                jsonSmtp.getString("recipient")
            )
        }
    }
}