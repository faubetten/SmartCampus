package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.smartcampus.client.ui.navigation.BottomNavItem
import com.smartcampus.client.ui.theme.SmartCampusClientTheme

@Composable
fun MainDashboardScreen(
    onLogout: () -> Unit,
    innerNavController: NavHostController = rememberNavController()
) {
    val items = listOf(BottomNavItem.Navigation, BottomNavItem.Scheduling)
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                innerNavController.navigate(item.route) {
                                    popUpTo(innerNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomNavItem.Navigation.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Navigation.route) {
                NavigationScreen(onLogout = onLogout)
            }
            composable(BottomNavItem.Scheduling.route) {
                SchedulingScreen(onLogout = onLogout)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    SmartCampusClientTheme {
        MainDashboardScreen(
            onLogout = {}
        )
    }
}