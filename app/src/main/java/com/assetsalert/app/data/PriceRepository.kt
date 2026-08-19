package com.assetsalert.app.data

import kotlinx.coroutines.flow.Flow

/** Result of one polling pass: alerts whose target price condition was just met. */
data class TriggerResult(val alert: Alert, val currentPrice: Double)

class PriceRepository(
    private val dao: AlertDao,
    private val settings: SettingsStore
) {
    fun observeAlerts(): Flow<List<Alert>> = dao.observeAll()

    suspend fun addAlert(alert: Alert): Long = dao.insert(alert)
    suspend fun updateAlert(alert: Alert) = dao.update(alert)
    suspend fun deleteAlert(alert: Alert) = dao.delete(alert)
    suspend fun rearm(alert: Alert) = dao.rearm(alert.id)

    /** Looks up matching coin ids on CoinGecko for a free-text query, e.g. "bitcoin". */
    suspend fun searchCrypto(query: String): List<CoinGeckoCoin> =
        runCatching { ApiClients.coinGecko.search(query).coins.take(10) }.getOrDefault(emptyList())

    /**
     * Polls the current price for every active, untriggered alert and returns
     * the ones that just crossed their target. Crypto alerts are batched into
     * a single CoinGecko call; stock alerts are queried individually against
     * Twelve Data (requires a user-supplied API key in Settings).
     */
    suspend fun pollAndCheck(): List<TriggerResult> {
        val active = dao.getActiveUntriggered()
        if (active.isEmpty()) return emptyList()

        val results = mutableListOf<TriggerResult>()

        val cryptoAlerts = active.filter { it.assetType == AssetType.CRYPTO }
        if (cryptoAlerts.isNotEmpty()) {
            val ids = cryptoAlerts.map { it.symbolId }.distinct().joinToString(",")
            val prices = runCatching { ApiClients.coinGecko.getPrices(ids) }.getOrNull()
            if (prices != null) {
                for (alert in cryptoAlerts) {
                    val price = prices[alert.symbolId]?.get("usd") ?: continue
                    dao.updatePrice(alert.id, price)
                    if (crossed(alert, price)) results += TriggerResult(alert, price)
                }
            }
        }

        val stockAlerts = active.filter { it.assetType == AssetType.STOCK }
        if (stockAlerts.isNotEmpty()) {
            val apiKey = settings.currentTwelveDataApiKey()
            if (!apiKey.isNullOrBlank()) {
                for (alert in stockAlerts) {
                    val resp = runCatching {
                        ApiClients.twelveData.getPrice(alert.symbolId, apiKey)
                    }.getOrNull() ?: continue
                    val price = resp.price?.toDoubleOrNull() ?: continue
                    dao.updatePrice(alert.id, price)
                    if (crossed(alert, price)) results += TriggerResult(alert, price)
                }
            }
        }

        for (r in results) dao.markTriggered(r.alert.id)
        return results
    }

    private fun crossed(alert: Alert, price: Double): Boolean = when (alert.direction) {
        Direction.ABOVE -> price >= alert.targetPrice
        Direction.BELOW -> price <= alert.targetPrice
    }
}
