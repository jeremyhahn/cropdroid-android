package com.jeremyhahn.cropdroid.config

import android.util.Log
import com.jeremyhahn.cropdroid.model.APIResponse
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class APIResponseParser {
    companion object {

        private val TAG = "APIResponseParser"
        fun parse(response: Response) : APIResponse {
            val responseBody = response.body().string()
            Log.d("APIResponseParser.parse", "response.body(): " + responseBody)
            return try {
                val jsonResponse = JSONObject(responseBody)
                val errcode = jsonResponse.getInt("code")
                val error = jsonResponse.getString("error")
                val success = jsonResponse.getBoolean("success")
                var payload = if(jsonResponse.isNull("payload")) null else jsonResponse.get("payload")
                var displayCode = if(errcode > 0) errcode else response.code()

                Log.d(TAG, "APIResponseParser payload: $payload")

                APIResponse(displayCode, error, success, payload)
            } catch(e: JSONException) {
                APIResponse(response.code(), responseBody, false, null)
            }
        }
    }
}
