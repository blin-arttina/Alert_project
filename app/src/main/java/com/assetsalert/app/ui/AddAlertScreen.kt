package com.assetsalert.app.ui

import androidx.compose.foundation.layout.*
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
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
