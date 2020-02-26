package com.jeremyhahn.cropdroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jeremyhahn.cropdroid.service.NotificationService

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("StartupReceiver.onReceive", "Restarting notification service")
        //context.startService(Intent(context, NotificationService::class.java))
        context.startForegroundService(intent)
    }
}