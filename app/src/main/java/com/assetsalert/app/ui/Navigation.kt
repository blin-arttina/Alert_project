package com.assetsalert.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object List : Screen("list")
    data object Add : Screen("add")
    data object Settings : Screen("settings")
}

@Composable
fun AssetsAlertNavHost(viewModel: AssetsAlertViewModel, startAtOnboarding: Boolean) {
    val nav: NavHostController = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = if (startAtOnboarding) Screen.Onboarding.route else Screen.List.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                viewModel.setHasSeenOnboarding(true)
                nav.navigate(Screen.List.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
            })
        }
        composable(Screen.List.route) {
            AlertListScreen(
                viewModel = viewModel,
                onAddClick = { nav.navigate(Screen.Add.route) },
                onSettingsClick = { nav.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Add.route) {
            AddAlertScreen(viewModel = viewModel, onDone = { nav.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }
    }
}
