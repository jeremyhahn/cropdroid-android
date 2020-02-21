package com.jeremyhahn.cropdroid.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jeremyhahn.cropdroid.model.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject


// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
class NotificationService : Service() {

    val CHANNEL_ID = "CROPDROID_NOTIFICATIONS"
    val GROUP_KEY = "CropDroid"

    val client = OkHttpClient()

    var userId : String? = null
    var controllerId : Int? = null
    var hostname : String? = null
    var jwt: String? = null

    var binder : IBinder? = null
    var websocket : WebSocket? = null

    var summaryNotificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null
    var bundleNotificationId : Int = 1

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

            val request = Request.Builder()
                .url("ws://".plus(hostname).plus("/api/v1/notification"))
                .addHeader("Authorization", "Bearer " + jwt)
                .build()
            val listener = NotificationWebSocketListener()
            websocket = client!!.newWebSocket(request, listener)
            client!!.dispatcher().executorService().shutdown()

            Toast.makeText(
                applicationContext,
                "NotificationService started.",
                Toast.LENGTH_LONG
            ).show()
            super.onStartCommand(intent, flags, startId)
            return START_REDELIVER_INTENT
        }
        stopSelf()
        return START_STICKY_COMPATIBILITY
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
                .setSmallIcon(R.mipmap.sym_def_app_icon)

        val newNotification =
            android.app.Notification.Builder(this, "channel_id")
                .setContentTitle(notification.type)
                .setContentText(notification.message)
                .setSmallIcon(R.mipmap.sym_def_app_icon)
                .setGroupSummary(false)
                .setGroup(GROUP_KEY)

        notificationManager!!.notify(notification.hashCode(), newNotification.build())
        notificationManager!!.notify(bundleNotificationId, summaryNotificationBuilder!!.build())
    }

    inner class NotificationWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
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
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("NotificationService.onFailure", response.toString())
            t.printStackTrace()
        }
    }
}
