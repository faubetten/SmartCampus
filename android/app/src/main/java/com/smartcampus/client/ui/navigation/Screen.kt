package com.smartcampus.client.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Navigation : Screen("navigation")
}