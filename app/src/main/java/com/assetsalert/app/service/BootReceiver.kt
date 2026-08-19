package com.assetsalert.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.assetsalert.app.AssetsAlertApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Restarts monitoring after a reboot if the user has any saved alerts. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as AssetsAlertApp
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hasAlerts = app.repository.observeAlerts().first().isNotEmpty()
                if (hasAlerts) PriceMonitorService.start(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
