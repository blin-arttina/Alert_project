package com.assetsalert.app

import android.app.Application
import com.assetsalert.app.data.AppDatabase
import com.assetsalert.app.data.PriceRepository
import com.assetsalert.app.data.SettingsStore

class AssetsAlertApp : Application() {
    val settings by lazy { SettingsStore(this) }
    val repository by lazy { PriceRepository(AppDatabase.get(this).alertDao(), settings) }
}
