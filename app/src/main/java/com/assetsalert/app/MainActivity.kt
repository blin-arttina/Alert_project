package com.assetsalert.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.assetsalert.app.ui.AssetsAlertNavHost
import com.assetsalert.app.ui.AssetsAlertViewModel
import com.assetsalert.app.ui.theme.AssetsAlertTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val viewModel: AssetsAlertViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the native splash screen up until we know whether to show onboarding,
        // so there's no blank flash between splash and first real content.
        var onboardingResolved = false
        splashScreen.setKeepOnScreenCondition { !onboardingResolved }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var showOnboarding by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                val hasSeen = viewModel.settings.hasSeenOnboarding.first()
                showOnboarding = !hasSeen
                onboardingResolved = true
            }

            showOnboarding?.let { startAtOnboarding ->
                val darkMode by viewModel.darkMode.collectAsState()
                AssetsAlertTheme(darkTheme = darkMode) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        AssetsAlertNavHost(viewModel = viewModel, startAtOnboarding = startAtOnboarding)
                    }
                }
            }
        }
    }
}
