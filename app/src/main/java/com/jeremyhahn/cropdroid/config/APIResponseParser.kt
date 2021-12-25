package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.APIResponse
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class APIResponseParser {
    companion object {
        fun parse(response: Response) : APIResponse {
            val responseBody = response.body().string()
            Log.d("APIResponseParser.parse", "response.body(): " + responseBody)
            try {
                val jsonResponse = JSONObject(responseBody)
                val error = jsonResponse.getString("error")
                val success = jsonResponse.getBoolean("success")
                val payload = jsonResponse.get("payload")
                return APIResponse(response.code(), error, success, payload)
            }
            catch(e: JSONException) {
                return APIResponse(response.code(), responseBody, false, "")
            }
        }
    }
}
