package com.assetsalert.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assetsalert.app.service.PriceMonitorService
import com.assetsalert.app.ui.theme.AssetsAlertTheme

/**
 * Launched via the triggered notification's full-screen intent (and shown
 * over the lock screen) so a fired alert is impossible to miss, matching
 * the "audible alarm system" behavior described in the product doc.
 */
class AlarmRingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val assetName = intent.getStringExtra(EXTRA_ASSET_NAME) ?: "Your asset"
        val targetPrice = intent.getDoubleExtra(EXTRA_TARGET_PRICE, 0.0)
        val currentPrice = intent.getDoubleExtra(EXTRA_CURRENT_PRICE, 0.0)

        setContent {
            val app = application as AssetsAlertApp
            val darkMode by app.settings.darkMode.collectAsState(initial = true)
            AssetsAlertTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AlarmRingContent(
                        assetName = assetName,
                        targetPrice = targetPrice,
                        currentPrice = currentPrice,
                        onStop = {
                            PriceMonitorService.stopAlarmOnly(this)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_ASSET_NAME = "asset_name"
        const val EXTRA_TARGET_PRICE = "target_price"
        const val EXTRA_CURRENT_PRICE = "current_price"
    }
}

@Composable
private fun AlarmRingContent(assetName: String, targetPrice: Double, currentPrice: Double, onStop: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Target hit!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(assetName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Now $%.2f  ·  target $%.2f".format(currentPrice, targetPrice),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = onStop, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Stop alarm", style = MaterialTheme.typography.titleMedium)
        }
    }
}
