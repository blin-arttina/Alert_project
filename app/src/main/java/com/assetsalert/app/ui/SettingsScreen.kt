package com.assetsalert.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AssetsAlertViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val darkMode by viewModel.darkMode.collectAsState()
    val customSoundUri by viewModel.settings.customSoundUri.collectAsState(initial = null)
    var apiKeyInput by remember { mutableStateOf("") }

    val soundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.setCustomSoundUri(uri.toString())
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Back") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Dark mode", modifier = Modifier.weight(1f))
                Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
            }

            Spacer(Modifier.height(24.dp))

            Text("Alert sound", style = MaterialTheme.typography.titleMedium)
            Text(
                if (customSoundUri != null) "Custom sound selected" else "Using default alarm sound",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = { soundPicker.launch(arrayOf("audio/*")) }) {
                    Text("Choose audio file")
                }
                Spacer(Modifier.width(8.dp))
                if (customSoundUri != null) {
                    TextButton(onClick = { viewModel.setCustomSoundUri(null) }) { Text("Reset to default") }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Stock prices (Twelve Data)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Free API key from twelvedata.com — required for stock alerts, not needed for crypto.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("API key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { scope.launch { viewModel.setTwelveDataApiKey(apiKeyInput) } }) {
                Text("Save key")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Monitoring runs in a foreground service and checks prices roughly every 30 seconds while alerts are active.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
