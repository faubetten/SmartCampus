package com.smartcampus.client.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object MainDashboard : Screen("dashboard")
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Navigation : BottomNavItem("dashboard/nav", "Navigation", Icons.Default.Place)
    data object Scheduling : BottomNavItem("dashboard/schedule", "Room Booking", Icons.Default.DateRange)
}