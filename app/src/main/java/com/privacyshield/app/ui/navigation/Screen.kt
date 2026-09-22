package com.privacyshield.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object Analysis : Screen("analysis")
    object Editor : Screen("editor")
    object Preview : Screen("preview")
    object Settings : Screen("settings")
}
