package com.jeremyhahn.cropdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jeremyhahn.cropdroid.MasterControllerListActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject


class NotificationService : Service() {

    val serviceType : String = "notification"
    val client = OkHttpClient()

    var userId : String? = null
    var controllerId : Int? = null
    var hostname : String? = null
    var jwt: String? = null

    var binder : IBinder? = null
    var serviceRecord: com.jeremyhahn.cropdroid.model.Service? = null
    var websocket : WebSocket? = null

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && websocket == null) {
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

    fun sendNotification(notification: Notification) {
        val intent = Intent(applicationContext, MasterControllerListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        createNotificationChannel(applicationContext,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            applicationContext.getString(com.jeremyhahn.cropdroid.R.string.app_name), "CropDroid notification channel.")

        val channelId = "${applicationContext.packageName}-${applicationContext.getString(com.jeremyhahn.cropdroid.R.string.app_name)}"
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(notification.type)
            setContentText(notification.controller)
            setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(pendingIntent)
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1001, notificationBuilder.build())
    }

    fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {
        // 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // 2
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            // 3
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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
            sendNotification(notification)
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
