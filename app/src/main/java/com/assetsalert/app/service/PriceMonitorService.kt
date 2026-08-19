package com.assetsalert.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.assetsalert.app.AssetsAlertApp
import com.assetsalert.app.data.PriceRepository
import com.assetsalert.app.data.SettingsStore
import com.assetsalert.app.data.TriggerResult
import com.assetsalert.app.notification.Notifications
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class PriceMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: PriceRepository
    private lateinit var settings: SettingsStore
    private lateinit var alarmPlayer: AlarmPlayer
    private var pollJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as AssetsAlertApp
        repository = app.repository
        settings = app.settings
        alarmPlayer = AlarmPlayer(this)
        Notifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                alarmPlayer.stop()
                NotificationManagerCompat.from(this).cancel(ALERT_NOTIFICATION_ID)
                return START_STICKY
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForeground(Notifications.MONITOR_NOTIFICATION_ID, Notifications.monitoringNotification(this, 0))
        if (pollJob == null) startPollingLoop()
        return START_STICKY
    }

    private fun startPollingLoop() {
        pollJob = scope.launch {
            while (isActive) {
                val results = runCatching { repository.pollAndCheck() }.getOrDefault(emptyList())
                if (results.isNotEmpty()) handleTriggers(results)
                val intervalSeconds = settings.currentPollIntervalSeconds().coerceAtLeast(10)
                delay(intervalSeconds * 1000L)
            }
        }
    }

    private suspend fun handleTriggers(results: List<TriggerResult>) {
        val customSound = settings.customSoundUri.first()
        withContext(Dispatchers.Main) { alarmPlayer.start(customSound) }

        val stopIntent = Intent(this, PriceMonitorService::class.java).apply { action = ACTION_STOP_ALARM }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = NotificationManagerCompat.from(this)
        for (r in results) {
            nm.notify(
                ALERT_NOTIFICATION_ID,
                Notifications.triggeredNotification(this, r.alert, r.currentPrice, stopPending)
            )
        }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        alarmPlayer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP_ALARM = "com.assetsalert.app.STOP_ALARM"
        const val ACTION_STOP_SERVICE = "com.assetsalert.app.STOP_SERVICE"
        const val ALERT_NOTIFICATION_ID = 2001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, PriceMonitorService::class.java))
        }

        fun stop(context: Context) {
            context.startService(Intent(context, PriceMonitorService::class.java).apply {
                action = ACTION_STOP_SERVICE
            })
        }

        /** Stops just the ringing alarm (called from AlarmRingActivity's Stop button) without killing monitoring. */
        fun stopAlarmOnly(context: Context) {
            context.startService(Intent(context, PriceMonitorService::class.java).apply {
                action = ACTION_STOP_ALARM
            })
        }
    }
}
