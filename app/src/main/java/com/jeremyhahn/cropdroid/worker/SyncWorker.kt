package com.jeremyhahn.cropdroid.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jeremyhahn.cropdroid.MasterControllerListActivity

class SyncWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private var notificationManager: NotificationManager? = null

    override fun doWork(): Result {
        Log.d("SyncWorker", "doWork called")
        sendNotification(context)
        return Result.success()
    }

    fun sendNotification(context : Context) {
        val intent = Intent(context, MasterControllerListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        createNotificationChannel(context,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            context.getString(com.jeremyhahn.cropdroid.R.string.app_name), "CropDroid notification channel.")


        val channelId = "${context.packageName}-${context.getString(com.jeremyhahn.cropdroid.R.string.app_name)}"
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(com.jeremyhahn.cropdroid.R.drawable.ic_launcher_background) // 3
            setContentTitle("CropDroid") // 4
            setContentText("Test message!") // 5
            setStyle(NotificationCompat.BigTextStyle().bigText("BigText")) // 6
            priority = NotificationCompat.PRIORITY_DEFAULT // 7
            setAutoCancel(true) // 8
            setContentIntent(pendingIntent)
        }

        val notificationManager = NotificationManagerCompat.from(context)
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
}