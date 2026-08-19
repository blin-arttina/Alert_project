package com.assetsalert.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetType { CRYPTO, STOCK }
enum class Direction { ABOVE, BELOW }

/**
 * A single watched asset + target price. `symbolId` is the CoinGecko coin id
 * (e.g. "bitcoin") for CRYPTO, or the ticker (e.g. "AAPL") for STOCK.
 */
@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbolId: String,
    val displayName: String,
    val assetType: AssetType,
    val targetPrice: Double,
    val direction: Direction,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val lastKnownPrice: Double? = null
)
