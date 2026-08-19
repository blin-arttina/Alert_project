package com.assetsalert.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.assetsalert.app.AssetsAlertApp
import com.assetsalert.app.data.*
import com.assetsalert.app.service.PriceMonitorService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssetsAlertViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx = app as AssetsAlertApp
    private val repo = appCtx.repository
    val settings = appCtx.settings

    val alerts: StateFlow<List<Alert>> = repo.observeAlerts()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val darkMode: StateFlow<Boolean> = settings.darkMode
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    fun addAlert(symbolId: String, displayName: String, type: AssetType, target: Double, direction: Direction) {
        viewModelScope.launch {
            repo.addAlert(
                Alert(
                    symbolId = symbolId,
                    displayName = displayName,
                    assetType = type,
                    targetPrice = target,
                    direction = direction
                )
            )
            PriceMonitorService.start(getApplication())
        }
    }

    fun deleteAlert(alert: Alert) = viewModelScope.launch { repo.deleteAlert(alert) }

    fun rearmAlert(alert: Alert) = viewModelScope.launch { repo.rearm(alert) }

    fun toggleActive(alert: Alert) = viewModelScope.launch {
        repo.updateAlert(alert.copy(isActive = !alert.isActive))
    }

    fun setDarkMode(v: Boolean) = viewModelScope.launch { settings.setDarkMode(v) }

    fun setHasSeenOnboarding(v: Boolean) = viewModelScope.launch { settings.setHasSeenOnboarding(v) }

    fun setCustomSoundUri(uri: String?) = viewModelScope.launch { settings.setCustomSoundUri(uri) }

    fun setTwelveDataApiKey(key: String) = viewModelScope.launch { settings.setTwelveDataApiKey(key) }

    suspend fun searchCrypto(query: String): List<CoinGeckoCoin> = repo.searchCrypto(query)

    fun stopMonitoring() = PriceMonitorService.stop(getApplication())
    fun startMonitoring() = PriceMonitorService.start(getApplication())
}
