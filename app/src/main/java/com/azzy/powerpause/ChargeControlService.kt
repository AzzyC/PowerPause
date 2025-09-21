package com.azzy.powerpause

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ChargeControlService : Service() {

    // Coroutine scope for background looping
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        val channelId = "powerpause_channel"

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PowerPause Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // Create persistent notification for foreground service
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PowerPause Running")
            .setContentText("Controlling charging...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Read values passed from MainActivity
        val bypassMode = intent?.getBooleanExtra("bypass_mode", false) ?: false
        val batteryLimit = intent?.getIntExtra("battery_limit", 0) ?: 0

        scope.launch {
            if (bypassMode) {
                // Keep sending 1 to stop charging continuously
                while (isActive) {
                    writeToControlFile(1)
                    delay(5000) // wait 5 seconds before next write
                }
            } else {
                // Monitor battery and enforce limit
                while (isActive) {
                    val current = getBatteryPercent()
                    if (current >= batteryLimit) writeToControlFile(1)
                    else writeToControlFile(0)
                    delay(5000)
                }
            }
        }

        return START_STICKY
    }

    // Write integer to sysfs control file to stop/start charging
    private fun writeToControlFile(value: Int) {
        try {
            val path = "/sys/class/qcom-battery/input_suspend"
            val file = java.io.File(path)
            file.writeText(value.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Read current battery percentage from sysfs
    private fun getBatteryPercent(): Int {
        return try {
            val path = "/sys/class/power_supply/battery/capacity"
            val file = java.io.File(path)
            file.readText().trim().toInt()
        } catch (e: Exception) {
            0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Cancel all background coroutines
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
