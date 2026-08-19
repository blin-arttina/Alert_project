package com.assetsalert.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.assetsalert.app.R
import com.assetsalert.app.data.Alert
import android.app.PendingIntent
import android.content.Intent

object Notifications {
    const val MONITOR_CHANNEL = "monitor_channel"
    const val ALERT_CHANNEL = "alert_channel"
    const val MONITOR_NOTIFICATION_ID = 1001

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                MONITOR_CHANNEL, "Background monitoring", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shown while Assets Alert is watching prices" }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                ALERT_CHANNEL, "Price alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sounds when a target price is hit"
                enableVibration(true)
            }
        )
    }

    fun monitoringNotification(context: Context, watchedCount: Int) =
        NotificationCompat.Builder(context, MONITOR_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Assets Alert is watching $watchedCount asset(s)")
            .setContentText("You'll be notified the moment a target price is hit")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    fun triggeredNotification(context: Context, alert: Alert, currentPrice: Double, dismissIntent: PendingIntent): android.app.Notification {
        val fullScreenIntent = Intent(context, com.assetsalert.app.AlarmRingActivity::class.java).apply {
            putExtra(com.assetsalert.app.AlarmRingActivity.EXTRA_ASSET_NAME, alert.displayName)
            putExtra(com.assetsalert.app.AlarmRingActivity.EXTRA_TARGET_PRICE, alert.targetPrice)
            putExtra(com.assetsalert.app.AlarmRingActivity.EXTRA_CURRENT_PRICE, currentPrice)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPending = PendingIntent.getActivity(
            context, alert.id.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ALERT_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${alert.displayName} hit your target")
            .setContentText("Now $%.2f (target $%.2f)".format(currentPrice, alert.targetPrice))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPending, true)
            .setContentIntent(fullScreenPending)
            .addAction(0, "Stop alarm", dismissIntent)
            .build()
    }
}
