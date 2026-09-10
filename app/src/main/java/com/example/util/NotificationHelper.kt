package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val CHANNEL_ALERTS = "data_guard_alerts"
    private const val CHANNEL_NAME = "Data Guard Alerts"
    private const val CHANNEL_DESC = "Notifications for data usage warnings, runway limits, and high usage alerts"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ALERTS, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun sendUsageAlert(
        context: Context,
        id: Int,
        title: String,
        message: String
    ) {
        if (!PermissionUtils.hasNotificationPermission(context)) return

        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_data_guard)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission might have been revoked
        }
    }

    fun sendRunwayWarning(context: Context, shortageDays: Int) {
        sendUsageAlert(
            context = context,
            id = 1001,
            title = "⚠️ Data Runway Warning",
            message = "You're projected to run out about $shortageDays days before your plan renews."
        )
    }

    fun sendHighAppUsageAlert(context: Context, appName: String, usedBytes: Long) {
        val formatted = DataFormatUtils.formatBytes(usedBytes)
        sendUsageAlert(
            context = context,
            id = 1002,
            title = "🔴 High Data Usage",
            message = "$appName used $formatted today. That's your biggest mobile-data user today."
        )
    }

    fun sendUnusualPaceAlert(context: Context, todayBytes: Long, normalBytes: Long) {
        val todayStr = DataFormatUtils.formatBytes(todayBytes)
        val normalStr = DataFormatUtils.formatBytes(normalBytes)
        sendUsageAlert(
            context = context,
            id = 1003,
            title = "⚠️ Fast Data Consumption",
            message = "Your data is being used faster than normal. $todayStr used today (your normal: $normalStr)."
        )
    }
}
