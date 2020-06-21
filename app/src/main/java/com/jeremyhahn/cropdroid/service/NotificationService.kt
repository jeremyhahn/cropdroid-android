package com.jeremyhahn.cropdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.ClientConfig
import com.jeremyhahn.cropdroid.model.Notification
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.lang.Thread.sleep
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NotificationService : Service() {

    val GROUP_KEY_FOREGROUND = "cropdroid_foreground"
    val CONNECTION_FAILED_DELAY = 60000L  // one minute
    var binder : IBinder? = null
    var websockets : HashMap<ClientConfig, WebSocket> = HashMap()
    var notificationManager: NotificationManager? = null
    var bundlerNotificationBuilder: NotificationCompat.Builder? = null

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onBind(intent: Intent): IBinder {
        return binder!!
    }

    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        var contentText = "Monitoring and listening for notifications"
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val foregroundChannel = NotificationChannel("foreground_channel_id", "High priority notifications", NotificationManager.IMPORTANCE_HIGH)
        foregroundChannel.enableLights(true)
        foregroundChannel.enableVibration(true)
        foregroundChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE)
        notificationManager!!.createNotificationChannel(foregroundChannel)

        val _notification:  android.app.Notification = android.app.Notification.Builder(this, "foreground_channel_id")
            .setOngoing(true)
            .setGroup(GROUP_KEY_FOREGROUND)
            .setContentTitle("Real-time Protection")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_cropdroid_logo)
            .setContentIntent(pendingIntent)
            .setTicker(contentText) // audibly announce when accessibility services turned on
            .build()

        startForeground(1, _notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent!!.action != null && intent!!.action.equals(Constants.ACTION_STOP_SERVICE)) {
            Log.i("NotificationService.onStartCommand", "Stopping service");
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        if(!startListeners()) {
            return START_NOT_STICKY
        }

        //super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("NotificationService.onDestroy", "Service destroyed")
        //Log.d("NotificationService.onDestroy", "Starting new NotificationService intent")
        //notificationManager = null
        //sendBroadcast(Intent(this, NotificationService::class.java))
    }

    fun startListeners() : Boolean {
        var controllers = MasterControllerRepository(this).allControllers

        if(controllers.size <= 0) {
            Log.d("NotificationService.onStartCommand", "No controllers configured, aborting...")
            return false
        }

        val authenticatedControllers = ArrayList<ClientConfig>(controllers.size)

        for(controller in controllers) {
            if(websockets[controller] == null) {
                if(controller.token.isEmpty()) {
                    // Controller hasn't been logged into yet
                    continue
                }
                Log.d("NotificationService.onStartCommand", "Found controller: " + controller)
                authenticatedControllers.add(controller)
            }
        }

        if(authenticatedControllers.size <= 0) {
            return false
        }

        for(controller in authenticatedControllers) {
            if(websockets.get(controller) == null) {
                createWebsocket(controller)
            }
        }

        Log.d("NotificationService", "Connected to " + websockets.size.toString() + " controller(s)")

        return true
    }

    fun stopService() {
        var controllers = MasterControllerRepository(this).allControllers
        for(controller in controllers) {
            websockets[controller]!!.close(NORMAL_CLOSURE_STATUS, "EVENT_APP_CLOSE")
            websockets.remove(controller)
        }
        stopForeground(true)
        stopSelf()
    }

    fun createWebsocket(controller: ClientConfig) {
        val jwt = JsonWebToken(applicationContext, controller.token)
        for(org in jwt.organizations()) {
            for(farm in org.farms) {
                try {
                    val client = OkHttpClient()
                    val protocol = if (controller.secure == 1) "wss://" else "ws://"
                    val request = Request.Builder()
                        .url(protocol.plus(controller.hostname).plus(API_BASE).plus("/farms/${farm.id}/notifications"))
                        .addHeader("Authorization", "Bearer " + controller.token)
                        .build()
                    val listener = NotificationWebSocketListener()
                    websockets[controller] = client.newWebSocket(request, listener)
                    client.dispatcher().executorService().shutdown()
                    client.retryOnConnectionFailure()
                }
                catch(e: java.lang.IllegalArgumentException) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                }
                Log.d("NotificationService.createWebsocket", "Created WebSocket " + websockets[controller].hashCode() + " for " + controller.hostname)
            }
        }
    }

    fun createNotification(notification: Notification) {

        var unread = "You have unread messages"

        // Update the bundle notification every time a new notification comes up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager!!.notificationChannels.size < 5) {

                val groupChannel = NotificationChannel("bundle_channel_id", "Notification bundler", NotificationManager.IMPORTANCE_LOW)
                groupChannel.setShowBadge(true)
                groupChannel.enableLights(true)
                groupChannel.enableVibration(true)
                groupChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE)
                notificationManager!!.createNotificationChannel(groupChannel)

                val lowPriorityChannel = NotificationChannel("low_priority_channel_id", "Low priority notifications", NotificationManager.IMPORTANCE_LOW)
                lowPriorityChannel.setShowBadge(true)
                lowPriorityChannel.enableLights(true)
                lowPriorityChannel.enableVibration(false)
                lowPriorityChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE)
                lowPriorityChannel.setLightColor(Color.GREEN)
                notificationManager!!.createNotificationChannel(lowPriorityChannel)

                val medPriorityChannel = NotificationChannel("med_priority_channel_id", "Medium priority notifications", NotificationManager.IMPORTANCE_DEFAULT)
                medPriorityChannel.setShowBadge(true)
                medPriorityChannel.enableLights(true)
                medPriorityChannel.enableVibration(false)
                medPriorityChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE)
                medPriorityChannel.setLightColor(Color.YELLOW)
                notificationManager!!.createNotificationChannel(medPriorityChannel)

                val highPriorityChannel = NotificationChannel("high_priority_channel_id", "High priority notifications", NotificationManager.IMPORTANCE_HIGH)
                highPriorityChannel.setShowBadge(true)
                highPriorityChannel.enableLights(true)
                highPriorityChannel.enableVibration(true)
                highPriorityChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                highPriorityChannel.setLightColor(Color.RED)
                notificationManager!!.createNotificationChannel(highPriorityChannel)
            }
        }

        bundlerNotificationBuilder =
            NotificationCompat.Builder(this, "bundle_channel_id")
                .setGroup(notification.controller)
                .setGroupSummary(true)
                .setContentTitle(notification.controller)
                .setContentText(unread)
                .setSmallIcon(R.drawable.ic_sprout)
                .setTicker(unread)

        val newNotification =
            NotificationCompat.Builder(this, "low_priority_channel_id")
                .setContentTitle(notification.controller)
                .setContentText(notification.type)
                .setSmallIcon(R.drawable.ic_sprout)
                .setGroup(notification.controller)
                .setGroupSummary(false)
                .setWhen(Date.from(ZonedDateTime.parse(notification.timestamp).toInstant()).time)
                .setGroup(notification.controller)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setTicker(notification.message)  // audibly announce when accessibility services turned on
                .setLights(Color.GREEN, 3000, 3000)

        if(notification.priority == Constants.NOTIFICATION_PRIORITY_HIGH) {
            newNotification.setChannelId("high_priority_channel_id")
            newNotification.setSmallIcon(android.R.drawable.ic_dialog_alert)
            newNotification.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            newNotification.setLights(Color.RED, 3000, 3000)
            newNotification.setSound(DEFAULT_NOTIFICATION_URI)
            newNotification.setGroup(GROUP_KEY_FOREGROUND)
        } else if(notification.priority == Constants.NOTIFICATION_PRIORITY_MED) {
            newNotification.setChannelId("med_priority_channel_id")
            newNotification.setSmallIcon(android.R.drawable.ic_dialog_info)
            newNotification.setLights(Color.YELLOW, 3000, 3000)
        }

        notificationManager!!.notify(notification.hashCode(), newNotification.build())
        notificationManager!!.notify(notification.controller.hashCode(), bundlerNotificationBuilder!!.build())
    }

    inner class NotificationWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                val jws = JsonWebToken(applicationContext, controller.token)
                var payload = "{\"id\":" + jws.uid().toString() + "}"
                Log.d("NotificationService.onOpen", "controller="  + controller.hostname + ", payload=" + payload)
                webSocket.send(payload)
                createNotification(Notification(controller.hostname,
                    Constants.NOTIFICATION_PRIORITY_LOW,
                    "Notification Service",
                    "Listening for new notifications",
                    ZonedDateTime.now().toString()))
                return
            }
            //webSocket.send(ByteString.decodeHex("deadbeef"))
            //webSocket.close(Companion.NORMAL_CLOSURE_STATUS, "Goodbye !")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("NotificationService.onMessage(text)", text)
            var json = JSONObject(text)
            var notification = Notification(
                json.getString("controller"),
                json.getInt("priority"),
                json.getString("type"),
                json.getString("message"),
                json.getString("timestamp"))

            createNotification(notification)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("NotificationService.onMessage(bytes)", bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NotificationService.NORMAL_CLOSURE_STATUS, null)
            Log.d("NotificationService.onClosing", "$code / $reason")

            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                createNotification(Notification(controller.hostname,
                    Constants.NOTIFICATION_PRIORITY_MED,
                    "Notification Service",
                    "Connection closed!",
                    ZonedDateTime.now().toString()))
                return
            }

            Log.d("NotificationService.onFailure", "Unable to locate controller for closed websocket connection: " + webSocket.hashCode())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {

            t.printStackTrace()

            Log.d("NotificationService.onFailure", "response: " + response.toString())
            Log.d("NotificationService.onFailure", "t:" + t.toString())

            sleep(CONNECTION_FAILED_DELAY)

            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                Log.d("NotificationService.onFailure", "Restarting connection for " + controller.hostname)

                createNotification(Notification(controller.hostname,
                    Constants.NOTIFICATION_PRIORITY_MED,
                    "Notification Service",
                    "Connection failed! \n\n" + t.message, ZonedDateTime.now().toString()))

                webSocket.cancel()

                // Get latest controller config; may have changed since service started running
                var controllers = MasterControllerRepository(applicationContext).allControllers
                if(controllers.size <= 0) {
                    return
                }
                for(c in controllers) {
                    if (controller.equals(c)) {
                        if (controller.token.isEmpty()) {
                            // Controller hasn't been logged into yet
                            return
                        }
                        Log.d("NotificationService.onStartCommand", "Found controller: " + controller)
                        createWebsocket(controller)
                    }
                }
                return
            }

            Log.d("NotificationService.onFailure", "Unable to locate controller for failed websocket connection: " + webSocket.hashCode())
        }

        fun getControllerByWebSocket(webSocket: WebSocket) : ClientConfig? {
            for((k, v) in websockets) {
                if(v.equals(webSocket)) {
                    return k
                }
            }
            return null
        }
    }
}
