package com.gis.smartfinance.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Insights : Screen("insights")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
}