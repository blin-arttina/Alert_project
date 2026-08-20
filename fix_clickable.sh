#!/data/data/com.termux/files/usr/bin/bash
# Fixes the invalid clickable() call syntax. Run from inside Alert_project:
#   bash fix_clickable.sh
set -e
echo "Applying clickable syntax fix..."

cat > "app/src/main/java/com/assetsalert/app/ui/AddAlertScreen.kt" << 'FILEEOF'
package com.assetsalert.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.assetsalert.app.data.AssetType
import com.assetsalert.app.data.CoinGeckoCoin
import com.assetsalert.app.data.Direction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertScreen(viewModel: AssetsAlertViewModel, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var assetType by remember { mutableStateOf(AssetType.CRYPTO) }
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<CoinGeckoCoin>>(emptyList()) }
    var selectedCrypto by remember { mutableStateOf<CoinGeckoCoin?>(null) }
    var stockTicker by remember { mutableStateOf("") }
    var targetPrice by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(Direction.ABOVE) }

    Scaffold(topBar = { TopAppBar(title = { Text("New alert") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            SingleChoiceSegmented(
                options = listOf("Crypto" to AssetType.CRYPTO, "Stock" to AssetType.STOCK),
                selected = assetType,
                onSelect = { assetType = it; selectedCrypto = null; query = ""; searchResults = emptyList() }
            )

            Spacer(Modifier.height(16.dp))

            if (assetType == AssetType.CRYPTO) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        selectedCrypto = null
                        scope.launch {
                            searchResults = if (it.length >= 2) viewModel.searchCrypto(it) else emptyList()
                        }
                    },
                    label = { Text("Search coin (e.g. bitcoin)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (searchResults.isNotEmpty() && selectedCrypto == null) {
                    LazyColumn(Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { coin ->
                            ListItem(
                                headlineContent = { Text("${coin.name} (${coin.symbol.uppercase()})") },
                                modifier = Modifier.clickableRow {
                                    selectedCrypto = coin
                                    query = coin.name
                                    searchResults = emptyList()
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = stockTicker,
                    onValueChange = { stockTicker = it.uppercase() },
                    label = { Text("Ticker symbol (e.g. AAPL)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Stock prices require a free Twelve Data API key — add yours in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = targetPrice,
                onValueChange = { targetPrice = it },
                label = { Text("Target price (USD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            SingleChoiceSegmented(
                options = listOf("Price rises to" to Direction.ABOVE, "Price falls to" to Direction.BELOW),
                selected = direction,
                onSelect = { direction = it }
            )

            Spacer(Modifier.height(24.dp))

            val canSave = targetPrice.toDoubleOrNull() != null &&
                    (assetType == AssetType.CRYPTO && selectedCrypto != null ||
                     assetType == AssetType.STOCK && stockTicker.isNotBlank())

            Button(
                onClick = {
                    val price = targetPrice.toDouble()
                    if (assetType == AssetType.CRYPTO) {
                        selectedCrypto?.let {
                            viewModel.addAlert(it.id, "${it.name} (${it.symbol.uppercase()})", assetType, price, direction)
                        }
                    } else {
                        viewModel.addAlert(stockTicker, stockTicker, assetType, price, direction)
                    }
                    onDone()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save alert") }
        }
    }
}

@Composable
private fun <T> SingleChoiceSegmented(options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    Row {
        options.forEach { (label, value) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
FILEEOF
echo "  wrote AddAlertScreen.kt"

cat > "app/src/main/java/com/assetsalert/app/ui/AlertListScreen.kt" << 'FILEEOF'
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
    this.clickable(onClick = onClick)
FILEEOF
echo "  wrote AlertListScreen.kt"

echo ""
echo "Done. Now run:"
echo "  git add -A"
echo "  git commit -m \"Fix clickable call syntax\""
echo "  git push"