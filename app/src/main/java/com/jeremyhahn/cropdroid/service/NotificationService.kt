package com.jeremyhahn.cropdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.lang.Thread.sleep
import java.time.ZonedDateTime

// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
class NotificationService : Service() {

    val CONNECTION_FAILED_DELAY = 60000L  // one minute

    var binder : IBinder? = null
    var userId : String = ""
    var websockets : HashMap<MasterController, WebSocket> = HashMap()

    var summaryNotificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onBind(intent: Intent): IBinder {
        return binder!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        userId = intent!!.getStringExtra("user_id")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        var controllers = MasterControllerRepository(this).allControllers
        for(controller in controllers) {
            if(websockets[controller] == null) {
                createWebsocket(controller)
            }
        }

        Toast.makeText(
            applicationContext,
            "NotificationService running",
            Toast.LENGTH_LONG
        ).show()

        Log.d("NotificationService", "Connection count: " + controllers.size.toString())

        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    fun createWebsocket(controller: MasterController) {
        val client = OkHttpClient()
        val protocol = if(controller.secure == 1) "wss://" else "ws://"
        val request = Request.Builder()
            .url(protocol.plus(controller.hostname).plus(API_BASE).plus("/notification"))
            .addHeader("Authorization", "Bearer " + controller.token)
            .build()
        val listener = NotificationWebSocketListener()
        websockets[controller] = client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()
        client.retryOnConnectionFailure()

        Log.d("NotificationService.createWebsocket", "Created WebSocket " + websockets[controller].hashCode() + " for " + controller.name)
    }

    fun createNotification(notification: Notification) {

        // Update the bundle notification every time a new notification comes up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager!!.notificationChannels.size < 2) {

                val groupChannel = NotificationChannel("bundle_channel_id", "bundle_channel_name", NotificationManager.IMPORTANCE_LOW)
                notificationManager!!.createNotificationChannel(groupChannel)

                val channel = NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager!!.createNotificationChannel(channel)
            }
        }

        summaryNotificationBuilder =
            NotificationCompat.Builder(this, "bundle_channel_id")
                //.setOngoing(true)
                .setGroup(notification.controller)
                .setGroupSummary(true)
                .setContentTitle(notification.controller)
                .setContentText("Notifications")
                .setSmallIcon(R.drawable.ic_cropdroid_logo)

        val newNotification =
            android.app.Notification.Builder(this, "channel_id")
                .setContentTitle(notification.controller)
                .setContentText(notification.message)
                .setSmallIcon(R.drawable.ic_cropdroid_logo)
                .setGroupSummary(false)
                .setGroup(notification.controller)

        notificationManager!!.notify(notification.hashCode(), newNotification.build())
        notificationManager!!.notify(notification.controller.hashCode(), summaryNotificationBuilder!!.build())
    }

    inner class NotificationWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                webSocket.send("{\"Id\":$userId}")
                createNotification(Notification(controller.name, controller.name, "Listening for new notifications", ZonedDateTime.now().toString()))
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
                createNotification(Notification(controller.name, "Notifications", "Connection closed!", ZonedDateTime.now().toString()))
                return
            }

            Log.d("NotificationService.onFailure", "Unable to locate controller for closed websocket connection: " + webSocket.hashCode())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("NotificationService.onFailure", response.toString())
            t.printStackTrace()

            sleep(CONNECTION_FAILED_DELAY)

            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                Log.d("NotificationService.onFailure", "Restarting connection for " + controller.name)
                createNotification(Notification(controller.name, "Notifications", "Connection failed!", ZonedDateTime.now().toString()))
                createWebsocket(controller)
                return
            }

            Log.d("NotificationService.onFailure", "Unable to locate controller for failed websocket connection: " + webSocket.hashCode())
        }

        fun getControllerByWebSocket(webSocket: WebSocket) : MasterController? {
            for((k, v) in websockets) {
                if(v.equals(webSocket)) {
                    return k
                }
            }
            return null
        }

        /*
        fun getWebsocketByControllerName(name: String) : WebSocket? {
            for((k, v) in websockets) {
                if(k.name == name) {
                    return v
                }
            }
            return null
        }*/
    }
}
