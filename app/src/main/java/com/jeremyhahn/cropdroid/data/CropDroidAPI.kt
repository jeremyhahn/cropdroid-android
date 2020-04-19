package com.jeremyhahn.cropdroid.data

import android.util.Log
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.Schedule
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CropDroidAPI(val controller: MasterController) {

    val REST_ENDPOINT: String
    val ROOM_RESOURCE = "/".plus(ControllerType.Room.name.toLowerCase())
    val RESERVOIR_RESOURCE = "/".plus(ControllerType.Reservoir.name.toLowerCase())
    val DOSER_RESOURCE = "/".plus(ControllerType.Doser.name.toLowerCase())
    val EVENTS_RESOURCE = "/events"
    val CONFIG_RESOURCE = "/config"
    val SCHEDULE_RESOURCE = "/schedule"
    val CHANNEL_RESOURCE = "/channels"
    val METRIC_RESOURCE = "/metrics"
    val VIRTUAL_RESOURCE = "/virtual"
    val ALGORITHMS_RESOURCE = "/algorithms"
    val ROOM_HISTORY_RESOURCE = ROOM_RESOURCE.plus("/history")
    val RESERVOIR_HISTORY_RESOURCE = RESERVOIR_RESOURCE.plus("/history")
    val CONTROLLER_RESOURCE = "/controllers"

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
        doGet(ROOM_RESOURCE.plus("/view"), args, callback)
    }
/*
    fun roomHistory(metric: String, callback: Callback) {
        var args = ArrayList<String>(0)
        args.add(metric)
        doGet(ROOM_HISTORY_RESOURCE, args, callback)
    }
*/
    fun reservoirStatus(callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(RESERVOIR_RESOURCE.plus("/view"), args, callback)
    }

    fun metricHistory(controllerType: ControllerType, metric: String, callback: Callback) {
        var args = ArrayList<String>(2)
        args.add("history")
        args.add(metric)
        doGet("/".plus(controllerType.name.toLowerCase()), args, callback)
    }

    fun doserStatus(callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(DOSER_RESOURCE.plus("/view"), args, callback)
    }

    fun dispense(controllerType: ControllerType, channelId: Int, seconds: Int, callback: Callback) {
        val resource = controllerType.name.toLowerCase()
        var args = ArrayList<String>(4)
        args.add("dispense")
        args.add(channelId.toString())
        args.add(seconds.toString())
        doGet(resource, args, callback)
    }

    fun switch(controllerType: ControllerType, channelId: Int, state: Boolean, callback: Callback) {
        val resource = "/".plus(controllerType.name.toLowerCase())
        var state = if(state) "1" else "0"
        var args = ArrayList<String>(4)
        args.add("switch")
        args.add(channelId.toString())
        args.add(state)
        doGet(resource, args, callback)
    }

    fun setConfig(controllerId: Int, key: String, value: String, callback: Callback) {
        var args = ArrayList<String>(3)
        args.add(controllerId.toString())
        args.add(key)
        args.add("?value="+URLEncoder.encode(value, "utf-8"))
        doGet(CONFIG_RESOURCE, args, callback)
    }

    fun getSchedule(channelId: Int, callback: Callback) {
        var args = ArrayList<String>(1)
        args.add("channel")
        args.add(channelId.toString())
        doGet(SCHEDULE_RESOURCE, args, callback)
    }

    fun createSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.createSchedule", "schedule="+schedule)

        val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
        formatter.calendar = schedule.startDate

        var json = JSONObject()
        json.put("id", schedule.id)
        json.put("channelId", schedule.channelId)
        json.put("startDate", formatter.format(schedule.startDate.time))
        if(schedule.endDate != null) {
            formatter.calendar = schedule.endDate
            json.put("endDate", formatter.format(schedule.endDate!!.time))
        }
        json.put("frequency", schedule.frequency)
        json.put("interval", schedule.interval)
        json.put("count", schedule.count)
        json.put("days", JSONArray(schedule.days))
        doPost(SCHEDULE_RESOURCE, json, callback)
    }

    fun updateSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.updateSchedule", "schedule="+schedule)

        val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
        formatter.calendar = schedule.startDate

        var json = JSONObject()
        json.put("id", schedule.id)
        json.put("channelId", schedule.channelId)
        json.put("startDate", formatter.format(schedule.startDate.time))
        if(schedule.endDate != null) {
            formatter.calendar = schedule.endDate
            json.put("endDate", formatter.format(schedule.endDate!!.time))
        }
        json.put("frequency", schedule.frequency)
        json.put("interval", schedule.interval)
        json.put("count", schedule.count)
        json.put("days", JSONArray(schedule.days))
        doPut(SCHEDULE_RESOURCE, json, callback)
    }

    fun deleteSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.deleteSchedule", "schedule="+schedule)
        val args = ArrayList<String>(1)
        args.add(schedule.id.toString())
        doDelete(SCHEDULE_RESOURCE, args, callback)
    }

    fun setMetricConfig(metric: Metric, callback: Callback) {
        Log.d("CropDropAPI.setMetricConfig", "metric="+metric)
        var json = JSONObject()
        json.put("id", metric.id)
        json.put("key", metric.key)
        json.put("name", metric.name)
        json.put("enable", metric.enable)
        json.put("notify", metric.notify)
        json.put("unit", metric.unit)
        json.put("alarmLow", metric.alarmLow)
        json.put("alarmHigh", metric.alarmHigh)
        doPut(METRIC_RESOURCE, json, callback)
    }

    fun setVirtualMetricValue(controllerType: ControllerType, metric: Metric, callback: Callback) {
        Log.d("CropDropAPI.setVirtualMetricValue", "metric="+metric)
        var args = ArrayList<String>(4)
        args.add(controllerType.name.toLowerCase())
        args.add(metric.key)
        args.add(metric.value.toString())
        doGet(VIRTUAL_RESOURCE, args, callback)
    }

    fun setChannelConfig(channel: Channel, callback: Callback) {
        Log.d("CropDropAPI.setChannelConfig", "channel="+channel)
        var json = JSONObject()
        json.put("id", channel.id)
        json.put("channelId", channel.channelId)
        json.put("name", channel.name)
        json.put("enable", channel.enable)
        json.put("notify", channel.notify)
        json.put("condition", channel.condition)
        //json.put("schedule", channel.schedule)
        json.put("duration", channel.duration)
        json.put("debounce", channel.debounce)
        json.put("backoff", channel.backoff)
        json.put("algorithmId", channel.algorithmId)
        doPut(CHANNEL_RESOURCE, json, callback)
    }

    fun getConfig(callback: Callback) {
        val args = ArrayList<String>()
        doGet(CONFIG_RESOURCE, args, callback)
    }

    fun getControllers(callback: Callback) {
        val args = ArrayList<String>()
        doGet(CONTROLLER_RESOURCE, args, callback)
    }

    fun getMetrics(controllerId: Int, callback: Callback) {
        val endpoint = METRIC_RESOURCE.plus("/").plus(controllerId)
        val args = ArrayList<String>()
        doGet(endpoint, args, callback)
    }

    fun getAlgorithms(callback: Callback) {
        val args = ArrayList<String>()
        doGet(ALGORITHMS_RESOURCE, args, callback)
    }

    fun login(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("email", username)
        json.put("password", password)
        doPost("/login", json, callback)
    }

    fun register(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("email", username)
        json.put("password", password)
        doPost("/register", json, callback)
    }

    fun doGet(resource: String, args: ArrayList<String>, callback: Callback) {
        if(controller.hostname.isEmpty()) return fail(callback, "Hostname required")
        var endpoint = REST_ENDPOINT.plus(resource)
        if(args.size > 0) {
            for(arg in args) {
                endpoint = endpoint.plus("/").plus(arg)
            }
        }
        Log.d("CropDroidAPI.doGet", "endpoint: " + endpoint)
        Log.d("CropDroidAPI.doGet", "token: " + controller.token)
        //val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        //val client = OkHttpClient.Builder()
        //.addInterceptor(logging)
        //  .build()
        val client = OkHttpClient()
        var request = Request.Builder()
            .header("Authorization","Bearer " + controller.token)
            .url(endpoint)
            .get()
            .build();
        try {
            client.newCall(request).enqueue(callback)
        }
        catch(e: java.net.ConnectException) {
            fail(callback, e.message!!)
        }
        catch(e: IOException) {
            e.printStackTrace()
            fail(callback, e.message!!)
        }
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
        try {
            client.newCall(request).enqueue(callback)
        }
        catch(e: java.net.ConnectException) {
            fail(callback, e.message!!)
        }
        catch(e: IOException) {
            e.printStackTrace()
            fail(callback, e.message!!)
        }
    }

    fun doPut(resource: String, json: JSONObject, callback: Callback) {
        if(controller.hostname.isEmpty()) return fail(callback, "Hostname required")
        var endpoint = REST_ENDPOINT.plus(resource)
        Log.d("CropDroidAPI.doPut", "endpoint: " + endpoint)
        var client = OkHttpClient()
        var JSON = MediaType.parse("application/json; charset=utf-8")
        var body = RequestBody.create(JSON, json.toString())
        var request: Request? = null
        if(controller.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .put(body)
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .put(body)
                .header("Authorization","Bearer " + controller.token)
                .build();
        }
        try {
            client.newCall(request).enqueue(callback)
        }
        catch(e: java.net.ConnectException) {
            fail(callback, e.message!!)
        }
        catch(e: IOException) {
            e.printStackTrace()
            fail(callback, e.message!!)
        }
    }

    fun doDelete(resource: String,  args: ArrayList<String>, callback: Callback) {
        if(controller.hostname.isEmpty()) return fail(callback, "Hostname required")
        var endpoint = REST_ENDPOINT.plus(resource)
        Log.d("CropDroidAPI.doDelete", "endpoint: " + endpoint)
        if(args.size > 0) {
            for(arg in args) {
                endpoint = endpoint.plus("/").plus(arg)
            }
        }
        var client = OkHttpClient()
        var request: Request? = null
        if(controller.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .delete()
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .delete()
                .header("Authorization","Bearer " + controller.token)
                .build();
        }
        try {
            client.newCall(request).enqueue(callback)
        }
        catch(e: java.net.ConnectException) {
            fail(callback, e.message!!)
        }
        catch(e: IOException) {
            e.printStackTrace()
            fail(callback, e.message!!)
        }
    }

    /*
    fun doFormPost(resource: String, args: Map<String, String>, callback: Callback) {
        val client = OkHttpClient()
        val formBuilder = FormBody.Builder()
        for((k, v) in args) {
            formBuilder.add(k, v)
        }
        val request: Request = Request.Builder()
            .url(REST_ENDPOINT.plus(resource))
            .header("Authorization","Bearer " + controller.token)
            .post(formBuilder.build())
            .build()
        try {
            client.newCall(request).enqueue(callback)
        }
        catch(e: IOException) {
            e.printStackTrace()
            fail(callback, e.message!!)
        }
    }
    */

    fun fail(callback: Callback, message: String) {
        callback.onFailure(null, IOException(message))
    }
}