package com.assetsalert.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assetsalert.app.data.Alert
import com.assetsalert.app.data.Direction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertListScreen(viewModel: AssetsAlertViewModel, onAddClick: () -> Unit, onSettingsClick: () -> Unit) {
    val alerts by viewModel.alerts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assets Alert") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add alert")
            }
        }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No alerts yet — tap + to watch your first asset", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        onToggle = { viewModel.toggleActive(alert) },
                        onDelete = { viewModel.deleteAlert(alert) },
                        onRearm = { viewModel.rearmAlert(alert) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun AlertRow(alert: Alert, onToggle: () -> Unit, onDelete: () -> Unit, onRearm: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(alert.displayName, fontWeight = FontWeight.SemiBold)
            val arrow = if (alert.direction == Direction.ABOVE) "≥" else "≤"
            Text(
                "Target $arrow $${"%.2f".format(alert.targetPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            alert.lastKnownPrice?.let {
                Text(
                    "Last seen: $${"%.2f".format(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (alert.isTriggered) {
                Text(
                    "Triggered — tap to re-arm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickableText(onRearm)
                )
            }
        }
        Switch(checked = alert.isActive, onCheckedChange = { onToggle() })
        TextButton(onClick = onDelete) { Text("Remove") }
    }
}

// small helper to avoid pulling in extra imports for a single clickable text
private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
