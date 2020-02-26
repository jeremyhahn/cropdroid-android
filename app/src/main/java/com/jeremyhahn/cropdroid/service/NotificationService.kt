package com.jeremyhahn.cropdroid.service

import android.app.*
import android.app.AlarmManager.ELAPSED_REALTIME
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.API_BASE
import com.jeremyhahn.cropdroid.MasterControllerListActivity
import com.jeremyhahn.cropdroid.MicroControllerActivity
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.lang.Thread.sleep
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
class NotificationService : Service() {

    val CONNECTION_FAILED_DELAY = 60000L  // one minute
    val GROUP_KEY = "cropdroid"

    var binder : IBinder? = null
    var websockets : HashMap<MasterController, WebSocket> = HashMap()

    //var summaryNotificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onBind(intent: Intent): IBinder {
        return binder!!
    }
/*
    override fun onTaskRemoved(rootIntent : Intent) {
        var restartService = Intent(getApplicationContext(), NotificationService::class.java)
        restartService.setPackage(getPackageName())
        var restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT)
        var alarmService = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI)
    }
*/
    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createForegroundNotification()
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent!!.action != null && intent!!.action.equals(Constants.STOP_SERVICE_ACTION)) {
            Log.i("NotificationService.onStartCommand", "Stopping service");
            stopService()
            return START_NOT_STICKY
        }

        var controllers = MasterControllerRepository(this).allControllers

        if(controllers.size <= 0) {
            Log.d("NotificationService.onStartCommand", "No controllers configured, aborting...")
            return START_NOT_STICKY
        }

        var authenticatedControllers = ArrayList<MasterController>(controllers.size)

        for(controller in controllers) {
            if(websockets[controller] == null) {
                if(controller.id == 0) {
                    // Controller hasn't been logged into yet
                    createForegroundNotification()
                    return START_NOT_STICKY
                }
                Log.d("NotificationService.onStartCommand", "Found controller: " + controller)
                authenticatedControllers.add(controller)
            }
        }

        for(controller in authenticatedControllers) {
            if(websockets.get(controller) == null) {
                createWebsocket(controller)
            }
        }

        /*
        Toast.makeText(
            applicationContext,
            "NotificationService running",
            Toast.LENGTH_LONG
        ).show()*/

        Log.d("NotificationService", "Connection count: " + controllers.size.toString())

        //super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("NotificationService.onDestroy", "Service destroyed")
        //Log.d("NotificationService.onDestroy", "Starting new NotificationService intent")
        //notificationManager = null
        //sendBroadcast(Intent(this, NotificationService::class.java))
    }

    private fun isRunning() : Boolean {
        return notificationManager != null
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

    fun createForegroundNotification() {

        val pendingIntent: PendingIntent =
            Intent(this, MasterControllerListActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val groupChannel = NotificationChannel("app_channel_id", "app_channel_id", NotificationManager.IMPORTANCE_LOW)
        groupChannel.setShowBadge(true)

        notificationManager!!.createNotificationChannel(groupChannel)

        val _notification:  android.app.Notification = android.app.Notification.Builder(this, "app_channel_id")
            //.setOngoing(true)
            //.setGroup(notification.controller)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setContentTitle("Notifications")
            .setContentText("Listening for incoming messages")
            .setSmallIcon(R.drawable.ic_sprout)
            .setContentIntent(pendingIntent)
            //.setTicker("Some ticker text...")
            .build()

        startForeground(1, _notification)
    }

    fun createNotification(notification: Notification) {

        // Update the bundle notification every time a new notification comes up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager!!.notificationChannels.size < 2) {

                //val groupChannel = NotificationChannel("bundle_channel_id", "bundle_channel_name", NotificationManager.IMPORTANCE_LOW)
                //groupChannel.setShowBadge(true)
                //notificationManager!!.createNotificationChannel(groupChannel)

                val channel = NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
                channel.setShowBadge(true)
                notificationManager!!.createNotificationChannel(channel)
            }
        }
/*
        summaryNotificationBuilder =
            NotificationCompat.Builder(this, "bundle_channel_id")
                //.setOngoing(true)
                .setGroup(notification.controller)
                .setGroupSummary(true)
                .setContentTitle(notification.controller)
                .setContentText("You have unread messages")
                .setSmallIcon(R.drawable.ic_cropdroid_logo)
*/

        val newNotification =
            NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(notification.controller)
                .setContentText(notification.type)
                .setSmallIcon(R.drawable.ic_cropdroid_logo)
                .setGroup(GROUP_KEY)
                .setGroupSummary(false)
                .setWhen(Date.from(ZonedDateTime.parse(notification.timestamp).toInstant()).time)
                //.setGroup(notification.controller)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))

        notificationManager!!.notify(notification.hashCode(), newNotification.build())
  //      notificationManager!!.notify(notification.controller.hashCode(), summaryNotificationBuilder!!.build())
    }

    inner class NotificationWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                var payload = "{\"id\":" + controller.userid.toString() + "}"
                Log.d("NotificationService.onOpen", "controller="  + controller.name + ", payload=" + payload)
                webSocket.send(payload)
                createNotification(Notification(controller.name, "Notification Service", "Listening for new notifications", ZonedDateTime.now().toString()))
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
                createNotification(Notification(controller.name, "Notification Service", "Connection closed!", ZonedDateTime.now().toString()))
                return
            }

            Log.d("NotificationService.onFailure", "Unable to locate controller for closed websocket connection: " + webSocket.hashCode())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("NotificationService.onFailure", t.message)
            t.printStackTrace()

            sleep(CONNECTION_FAILED_DELAY)

            var controller = getControllerByWebSocket(webSocket)
            if(controller != null) {
                Log.d("NotificationService.onFailure", "Restarting connection for " + controller.name)

                createNotification(Notification(controller.name, "Notification Service", "Connection failed! \n\n" + t.stackTrace, ZonedDateTime.now().toString()))
                webSocket.cancel()
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
