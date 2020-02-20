package com.jeremyhahn.cropdroid.data

import android.util.Log
import com.jeremyhahn.cropdroid.API_BASE
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class CropDroidAPI(val controller: String, useSSL: Boolean) {

    val REST_ENDPOINT: String
    val REST_ENDPOINT_SSL: String
    val USE_SSL: Boolean

    init {
        REST_ENDPOINT = "http://".plus(controller).plus(API_BASE)
        REST_ENDPOINT_SSL = "https://".plus(controller).plus(API_BASE)
        USE_SSL = useSSL
    }

    fun createEndpoint(resource: String) : String {
        if(USE_SSL) return REST_ENDPOINT_SSL.plus(resource) else {
            return REST_ENDPOINT.plus(resource)
        }
    }

    fun login(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("username", username)
        json.put("password", password)
        doPost("/login", json, callback)
    }

    fun register(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("username", username)
        json.put("password", password)
        doPost("/register", json, callback)
    }

    fun doPost(resource: String, json: JSONObject, callback: Callback) {

        if(controller.isEmpty()) return fail(callback, "Hostname required")

        var endpoint = createEndpoint(resource)

        Log.d("CropDroidAPI.doPost", "endpoint: " + endpoint)

        var client = OkHttpClient()
        var JSON = MediaType.parse("application/json; charset=utf-8")
        var body = RequestBody.create(JSON, json.toString())
        var request = Request.Builder()
             .url(endpoint)
             .post(body)
             .build();
        client.newCall(request).enqueue(callback)
    }

    fun fail(callback: Callback, message: String) {
        callback.onFailure(null, IOException(message))
    }
}