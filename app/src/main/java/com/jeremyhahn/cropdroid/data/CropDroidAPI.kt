package com.jeremyhahn.cropdroid.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.Purchase
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_ID_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ORG_ID_KEY
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.model.Connection
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat

class CropDroidAPI(private val connection: Connection, preferences: SharedPreferences) {

    val orgId: Int
    val farmId: Long

    val REST_ENDPOINT: String
    val VERSIONED_ENDPOINT: String
    val ORGANIZATION_ENDPOINT: String

    val ALGORITHMS_ENDPOINT: String
    val CHANNEL_ENDPOINT: String
    val CONFIG_ENDPOINT: String
    val CONDITION_ENDPOINT: String
    //val CONTROLLER_ENDPOINT: String
    val DEVICES_ENDPOINT: String
    val EVENTS_ENDPOINT: String
    val FARM_ENDPOINT: String
    val GOOGLE_ENDPOINT: String
    val IAP_ENDPOINT: String
    val METRICS_ENDPOINT: String
    val SCHEDULE_ENDPOINT: String

    init {
        orgId = preferences.getInt(CONFIG_ORG_ID_KEY, 0)
        farmId = preferences.getLong(CONFIG_FARM_ID_KEY, 0)

        REST_ENDPOINT = if(connection.secure == 1)
            "https://".plus(connection.hostname)
        else "http://".plus(connection.hostname)

        VERSIONED_ENDPOINT = REST_ENDPOINT.plus(API_BASE)
        ORGANIZATION_ENDPOINT = VERSIONED_ENDPOINT.plus("/organizations/").plus(orgId)

        //FARM_ENDPOINT = ORGANIZATION_ENDPOINT.plus("/farms/").plus(farmId)
        FARM_ENDPOINT = VERSIONED_ENDPOINT.plus("/farms/").plus(farmId)
        DEVICES_ENDPOINT = FARM_ENDPOINT.plus("/devices")

        METRICS_ENDPOINT = FARM_ENDPOINT.plus("/metrics")
        IAP_ENDPOINT = FARM_ENDPOINT.plus("/iap")

        EVENTS_ENDPOINT = FARM_ENDPOINT.plus("/events")
        CONFIG_ENDPOINT = FARM_ENDPOINT.plus("/config")
        CONDITION_ENDPOINT = FARM_ENDPOINT.plus("/conditions")
        SCHEDULE_ENDPOINT = FARM_ENDPOINT.plus("/schedule")
        CHANNEL_ENDPOINT = FARM_ENDPOINT.plus("/channels")
        ALGORITHMS_ENDPOINT = FARM_ENDPOINT.plus("/algorithms")
        //CONTROLLER_ENDPOINT = FARM_ENDPOINT.plus("/servers")
        GOOGLE_ENDPOINT = VERSIONED_ENDPOINT.plus("/google")
    }

    fun verifyPurchase(purchase: Purchase, callback: Callback) {
        Log.d("CropDropAPI.verifyPurchase", "purchase="+purchase)
        var json = JSONObject()
        json.put("orderId", purchase.orderId)
        json.put("productId", purchase.sku)
        json.put("purchaseToken", purchase.purchaseToken)
        json.put("purchaseTime", purchase.purchaseTime)
        doPost(IAP_ENDPOINT.plus("/verify"), json, callback)
    }

    fun eventsList(page: String, callback: Callback) {
        var args = ArrayList<String>(1)
        args.add(page)
        doGet(EVENTS_ENDPOINT, args, callback)
    }

    fun getMetricHistory(serverType: String, metric: String, callback: Callback) {
        var args = ArrayList<String>(2)
        args.add("history")
        args.add(metric)
        doGet(DEVICES_ENDPOINT.plus("/").plus(serverType), args, callback)
    }

    fun getState(serverType: String, callback: Callback) {
        var args = ArrayList<String>(0)
        doGet(DEVICES_ENDPOINT.plus("/").plus(serverType).plus("/view"), args, callback)
    }

    fun timerSwitch(serverType: String, channelId: Int, seconds: Int, callback: Callback) {
        val resource = DEVICES_ENDPOINT.plus("/").plus(serverType)
        var args = ArrayList<String>(4)
        args.add("timerSwitch")
        args.add(channelId.toString())
        args.add(seconds.toString())
        doGet(resource, args, callback)
    }

    fun switch(serverType: String, channelId: Int, state: Boolean, callback: Callback) {
        val resource = DEVICES_ENDPOINT.plus("/").plus(serverType)
        var state = if(state) "1" else "0"
        var args = ArrayList<String>(4)
        args.add("switch")
        args.add(channelId.toString())
        args.add(state)
        doGet(resource, args, callback)
    }

    fun setConfig(serverId: String, key: String, value: String, callback: Callback) {
        var args = ArrayList<String>(3)
        args.add(serverId)
        args.add(key)
        args.add("?value="+URLEncoder.encode(value, "utf-8"))
        doGet(CONFIG_ENDPOINT, args, callback)
    }

    fun getConditions(channelId: Long, callback: Callback) {
        var args = ArrayList<String>(1)
        args.add("channel")
        args.add(channelId.toString())
        doGet(CONDITION_ENDPOINT, args, callback)
    }

    fun createCondition(condition: ConditionConfig, callback: Callback) {
        Log.d("CropDropAPI.createCondition", "condition="+condition)
        var json = JSONObject()
        json.put("channel_id", condition.channelId)
        json.put("metric_id", condition.metricId)
        json.put("comparator", condition.comparator)
        json.put("threshold", condition.threshold)
        doPost(CONDITION_ENDPOINT, json, callback)
    }

    fun updateCondition(condition: ConditionConfig, callback: Callback) {
        Log.d("CropDropAPI.createCondition", "condition="+condition)
        var json = JSONObject()
        json.put("id", condition.id)
        json.put("channelId", condition.channelId)
        json.put("metricId", condition.metricId)
        json.put("comparator", condition.comparator)
        json.put("threshold", condition.threshold)
        doPut(CONDITION_ENDPOINT, json, callback)
    }

    fun deleteCondition(condition: ConditionConfig, callback: Callback) {
        Log.d("CropDropAPI.deleteCondition", "condition="+condition)
        val args = ArrayList<String>(1)
        args.add(condition.id)
        doDelete(CONDITION_ENDPOINT, args, callback)
    }

    fun getSchedule(channelId: Long, callback: Callback) {
        var args = ArrayList<String>(1)
        args.add("channel")
        args.add(channelId.toString())
        doGet(SCHEDULE_ENDPOINT, args, callback)
    }

    fun createSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.createSchedule", "schedule="+schedule)

        val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
        formatter.calendar = schedule.startDate

        var days: String? = schedule.days.joinToString(",")
        days = if(days == "") null else days

        var json = JSONObject()
        json.put("id", schedule.id)
        json.put("channel_id", schedule.channelId)
        json.put("startDate", formatter.format(schedule.startDate.time))
        if(schedule.endDate != null) {
            formatter.calendar = schedule.endDate
            json.put("endDate", formatter.format(schedule.endDate!!.time))
        }
        json.put("frequency", schedule.frequency)
        json.put("interval", schedule.interval)
        json.put("count", schedule.count)
        json.put("days", days)
        doPost(SCHEDULE_ENDPOINT, json, callback)
    }

    fun updateSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.updateSchedule", "schedule="+schedule)

        val formatter = SimpleDateFormat(Constants.DATE_FORMAT_RFC3339)
        formatter.calendar = schedule.startDate

        var days: String? = schedule.days.joinToString(",")
        days = if(days == "") null else days

        var json = JSONObject()
        json.put("id", schedule.id)
        json.put("channel_id", schedule.channelId)
        json.put("startDate", formatter.format(schedule.startDate.time))
        if(schedule.endDate != null) {
            formatter.calendar = schedule.endDate
            json.put("endDate", formatter.format(schedule.endDate!!.time))
        }
        json.put("frequency", schedule.frequency)
        json.put("interval", schedule.interval)
        json.put("count", schedule.count)
        json.put("days", days)
        doPut(SCHEDULE_ENDPOINT, json, callback)
    }

    fun deleteSchedule(schedule: Schedule, callback: Callback) {
        Log.d("CropDropAPI.deleteSchedule", "schedule="+schedule)
        val args = ArrayList<String>(1)
        args.add(schedule.id.toString())
        doDelete(SCHEDULE_ENDPOINT, args, callback)
    }

    fun setMetricConfig(metric: Metric, callback: Callback) {
        Log.d("CropDropAPI.setMetricConfig", "metric="+metric)
        var json = JSONObject()
        json.put("id", metric.id)
        json.put("controllerId", metric.controllerId)
        json.put("key", metric.key)
        json.put("name", metric.name)
        json.put("enable", metric.enable)
        json.put("notify", metric.notify)
        json.put("unit", metric.unit)
        json.put("alarmLow", metric.alarmLow)
        json.put("alarmHigh", metric.alarmHigh)
        doPut(METRICS_ENDPOINT, json, callback)
    }

    fun setMetricValue(serverType: String, metric: Metric, callback: Callback) {
        Log.d("CropDropAPI.setMetricValue", "metric="+metric)
        var args = ArrayList<String>(4)
        args.add(serverType)
        args.add("metrics")
        args.add(metric.key)
        args.add(metric.value.toString())
        doGet(DEVICES_ENDPOINT, args, callback)
    }

    fun setChannelConfig(channel: Channel, callback: Callback) {
        Log.d("CropDropAPI.setChannelConfig", "channel="+channel)
        var json = JSONObject()
        json.put("id", channel.id)
        json.put("controllerId", channel.controllerId)
        json.put("channelId", channel.channelId)
        json.put("name", channel.name)
        json.put("enable", channel.enable)
        json.put("notify", channel.notify)
        //json.put("schedule", channel.schedule)
        json.put("duration", channel.duration)
        json.put("debounce", channel.debounce)
        json.put("backoff", channel.backoff)
        json.put("algorithmId", channel.algorithmId)
        doPut(CHANNEL_ENDPOINT, json, callback)
    }

    fun getFarm(callback: Callback) {
        val args = ArrayList<String>()
        doGet(FARM_ENDPOINT, args, callback)
    }

    fun getConfig(callback: Callback) {
        val args = ArrayList<String>()
        doGet(CONFIG_ENDPOINT, args, callback)
    }

    fun getDevices(callback: Callback) {
        val args = ArrayList<String>()
        doGet(DEVICES_ENDPOINT, args, callback)
    }

    fun getMetrics(serverId: Long, callback: Callback) {
        val endpoint = METRICS_ENDPOINT.plus("/").plus(serverId)
        val args = ArrayList<String>()
        doGet(endpoint, args, callback)
    }

    fun getAlgorithms(callback: Callback) {
        val args = ArrayList<String>()
        doGet(ALGORITHMS_ENDPOINT, args, callback)
    }

    fun login(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("email", username)
        json.put("password", password)
        json.put("authType", 0)
        doPost(VERSIONED_ENDPOINT.plus("/login"), json, callback)
    }

    fun googleLogin(idToken: String, serverAuthCode: String, callback: Callback) {
        var json = JSONObject()
        json.put("email", idToken)
        json.put("password", serverAuthCode)
        json.put("authType", 1)
        doPost(GOOGLE_ENDPOINT.plus("/login"), json, callback)
    }

    fun register(username: String, password: String, callback: Callback) {
        if(username.isEmpty()) return fail(callback, "Username required")
        if(password.isEmpty()) return fail(callback, "Password required")
        var json = JSONObject()
        json.put("email", username)
        json.put("password", password)
        doPost(VERSIONED_ENDPOINT.plus("/register"), json, callback)
    }

    fun doGet(endpoint: String, args: ArrayList<String>, callback: Callback) {
        if(connection.hostname.isEmpty()) return fail(callback, "Hostname required")
        //var endpoint = REST_ENDPOINT.plus(resource)
        var endpoint = endpoint
        if(args.size > 0) {
            for(arg in args) {
                endpoint = endpoint.plus("/").plus(arg)
            }
        }
        Log.d("CropDroidAPI.doGet", "endpoint: " + endpoint)
        Log.d("CropDroidAPI.doGet", "token: " + connection.token)
        //val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        //val client = OkHttpClient.Builder()
        //.addInterceptor(logging)
        //  .build()
        val client = OkHttpClient()
        var request = Request.Builder()
            .header("Authorization","Bearer " + connection.token)
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

    fun doPost(endpoint: String, json: JSONObject, callback: Callback) {
        if(connection.hostname.isEmpty()) return fail(callback, "Hostname required")
        //var endpoint = REST_ENDPOINT.plus(resource)
        Log.d("CropDroidAPI.doPost", "endpoint: " + endpoint)
        var client = OkHttpClient()
        var JSON = MediaType.parse("application/json; charset=utf-8")
        var body = RequestBody.create(JSON, json.toString())
        var request: Request? = null
        if(connection.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .post(body)
                .header("Authorization","Bearer " + connection.token)
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

    fun doPut(endpoint: String, json: JSONObject, callback: Callback) {
        if(connection.hostname.isEmpty()) return fail(callback, "Hostname required")
        //var endpoint = REST_ENDPOINT.plus(resource)
        Log.d("CropDroidAPI.doPut", "endpoint: " + endpoint)
        var client = OkHttpClient()
        var JSON = MediaType.parse("application/json; charset=utf-8")
        var body = RequestBody.create(JSON, json.toString())
        var request: Request? = null
        if(connection.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .put(body)
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .put(body)
                .header("Authorization","Bearer " + connection.token)
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

    fun doDelete(endpoint: String,  args: ArrayList<String>, callback: Callback) {
        if(connection.hostname.isEmpty()) return fail(callback, "Hostname required")
        //var endpoint = REST_ENDPOINT.plus(resource)
        var endpoint = endpoint
        Log.d("CropDroidAPI.doDelete", "endpoint: " + endpoint)
        if(args.size > 0) {
            for(arg in args) {
                endpoint = endpoint.plus("/").plus(arg)
            }
        }
        var client = OkHttpClient()
        var request: Request? = null
        if(connection.token.isEmpty()) {
            request = Request.Builder()
                .url(endpoint)
                .delete()
                .build();
        }
        else {
            request = Request.Builder()
                .url(endpoint)
                .delete()
                .header("Authorization","Bearer " + connection.token)
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

    fun createWebsocket(context: Context, resource: String, listener: WebSocketListener): WebSocket? {
        Log.d("CropDroidAPI.createWebSocket", "resource: " + resource)
        try {
            val client = OkHttpClient()
            val protocol = if (connection.secure == 1) "wss://" else "ws://"
            var url = protocol.plus(connection.hostname).plus(API_BASE).plus(resource)
            Log.d("CropDroidAPI.createWebsocket", "Created WebSocket: " + url)
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + connection.token)
                .build()

            val websocket = client.newWebSocket(request, listener)
            //websockets[server] = HashMap()
            //websockets[server]!!.put(server.hostname, websocket)
            client.dispatcher().executorService().shutdown()
            client.retryOnConnectionFailure()
            return websocket
        }
        catch(e: java.lang.IllegalArgumentException) {
            com.jeremyhahn.cropdroid.AppError(context).toast(e.message!!)
        }
        return null
    }

/*
    fun getController(webSocket: WebSocket) : Connection? {
        for((k, v) in websockets) {
            if(v.equals(webSocket)) {
                return k
            }
        }
        return null
    }
*/
    /*
    fun doFormPost(resource: String, args: Map<String, String>, callback: Callback) {
        val client = OkHttpClient()
        val formBuilder = FormBody.Builder()
        for((k, v) in args) {
            formBuilder.add(k, v)
        }
        val request: Request = Request.Builder()
            .url(REST_ENDPOINT.plus(resource))
            .header("Authorization","Bearer " + server.token)
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