package com.jeremyhahn.cropdroid.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.jeremyhahn.cropdroid.model.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.lang.Thread.sleep
import java.time.ZonedDateTime

// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
class NotificationService : Service() {

    val CHANNEL_ID = "CROPDROID_NOTIFICATIONS"
    val GROUP_KEY = "CropDroid"

    var userId : String? = null
    var controllerId : Int? = null
    var hostname : String? = null
    var jwt: String? = null

    var binder : IBinder? = null
    var websocket : WebSocket? = null

    var summaryNotificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null
    var bundleNotificationId : Int = 1

    val MAX_SOCKET_ATTEMPTS = 10
    var socketFailures = 0

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onBind(intent: Intent): IBinder {
        return binder!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && websocket == null) {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            userId = intent.getStringExtra("user_id")
            controllerId = intent.getIntExtra("controller_id", 0)
            hostname = intent.getStringExtra("controller_hostname")
            jwt = intent.getStringExtra("jwt")

            Log.d("NotificationService.onStartCommand", "hostname: $hostname")
            Log.d("NotificationService.onStartCommand", "bearer token: $jwt")

            createWebsocket()

            Toast.makeText(
                applicationContext,
                "NotificationService started.",
                Toast.LENGTH_LONG
            ).show()
            super.onStartCommand(intent, flags, startId)
            return START_STICKY
        }
        stopSelf()
        return START_STICKY_COMPATIBILITY
    }

    fun createWebsocket() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://".plus(hostname).plus("/api/v1/notification"))
            .addHeader("Authorization", "Bearer " + jwt)
            .build()
        val listener = NotificationWebSocketListener()
        websocket = client!!.newWebSocket(request, listener)
        client!!.dispatcher().executorService().shutdown()
        client!!.retryOnConnectionFailure()
    }

    fun createNotification(notification: Notification) {

        //We need to update the bundle notification every time a new notification comes up.
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
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentTitle("CropDroid")
                .setContentText("You have unread messages")
                .setSmallIcon(R.drawable.ic_dialog_info)

        val newNotification =
            android.app.Notification.Builder(this, "channel_id")
                .setContentTitle(notification.type)
                .setContentText(notification.message)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setGroupSummary(false)
                .setGroup(GROUP_KEY)

        notificationManager!!.notify(notification.hashCode(), newNotification.build())
        notificationManager!!.notify(bundleNotificationId, summaryNotificationBuilder!!.build())
    }

    inner class NotificationWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            createNotification(Notification("Android", "Notifications", "Listening for new messages", ZonedDateTime.now().toString()))
            webSocket.send("{\"Id\":$userId}")
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

            createNotification(Notification("Android", "Notifications", "Socket closed!", ZonedDateTime.now().toString()))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("NotificationService.onFailure", response.toString())
            t.printStackTrace()

            createNotification(Notification("Android", "Notifications", "Disconnected from server", ZonedDateTime.now().toString()))
            sleep(60000)
            createWebsocket()
        }
    }
}
