package com.jeremyhahn.cropdroid.data

import android.util.Log
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.SwitchState
import com.jeremyhahn.cropdroid.model.MasterController
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class CropDroidAPI(val controller: MasterController) {

    val REST_ENDPOINT: String
    val ROOM_RESOURCE = ControllerType.Room.name.toLowerCase()
    val RESERVOIR_RESOURCE = ControllerType.Reservoir.name.toLowerCase()
    val DOSER_RESOURCE = ControllerType.Doser.name.toLowerCase()
    val EVENTS_RESOURCE = "events"

    init {
        REST_ENDPOINT = if(controller.secure == 1)
            "https://".plus(controller.hostname).plus(API_BASE)
        else "http://".plus(controller.hostname).plus(API_BASE)
    }

    fun eventsList(page: String, callback: Callback) {
        var args = ArrayList<String>(1)
        args.add(page)
        doGet(EVENTS_RESOURCE, args, callback)
    }

    fun roomStatus(callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(ROOM_RESOURCE, args, callback)
    }

    fun reservoirStatus(callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(RESERVOIR_RESOURCE, args, callback)
    }

    fun doserStatus(callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(DOSER_RESOURCE, args, callback)
    }

    fun switch(controllerType: ControllerType, channelId: Int, state: Boolean, callback: Callback) {
        val resource = controllerType.name.toLowerCase()
        var state = if(state) "1" else "0"
        var args = ArrayList<String>(4)
        args.add("switch")
        args.add(channelId.toString())
        args.add(state)
        doGet(resource, args, callback)
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

    fun doGet(resource: String, args: ArrayList<String>, callback: Callback) {
        if(controller.hostname.isEmpty()) return fail(callback, "Hostname required")

        var endpoint = REST_ENDPOINT.plus("/").plus(resource)
        if(args.size > 0) {
            for(arg in args) {
                endpoint = endpoint.plus("/").plus(arg)
            }
        }

        Log.d("CropDroidAPI.doGet", "endpoint: " + endpoint)
        Log.d("CropDroidAPI.doGet", "token: " + controller.token)

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        var request = Request.Builder()
            .header("Authorization","Bearer " + controller.token)
            .url(endpoint)
            .get()
            .build();

        client.newCall(request).enqueue(callback)
    }

    fun doPost(resource: String, json: JSONObject, callback: Callback) {

        if(controller.hostname.isEmpty()) return fail(callback, "Hostname required")

        var endpoint = REST_ENDPOINT.plus(resource)

        Log.d("CropDroidAPI.doPost", "endpoint: " + endpoint)

        var client = OkHttpClient()
        var JSON = MediaType.parse("application/json; charset=utf-8")
        var body = RequestBody.create(JSON, json.toString())
        var request: Request? = null

        if(controller.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .post(body)
                .header("Authorization","Bearer " + controller.token)
                .build();
        }
        client.newCall(request).enqueue(callback)
    }

    fun fail(callback: Callback, message: String) {
        callback.onFailure(null, IOException(message))
    }
}