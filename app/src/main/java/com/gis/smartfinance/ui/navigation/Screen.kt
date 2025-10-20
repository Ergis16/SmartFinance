package com.gis.smartfinance.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }
    object Insights : Screen("insights")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
}