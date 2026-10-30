package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.smartcampus.client.data.dto.RoomDetailsDto
import com.smartcampus.client.ui.navigation.BottomNavItem
import com.smartcampus.client.ui.theme.SmartCampusClientTheme

@Composable
fun MainDashboardScreen(
    onLogout: () -> Unit,
    innerNavController: NavHostController = rememberNavController()
) {
    // The dashboard keeps navigation and scheduling in one authenticated area.
    val items = listOf(BottomNavItem.Navigation, BottomNavItem.Scheduling)
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedTargetRoom by remember { mutableStateOf<RoomDetailsDto?>(null) }

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
                                // Preserve each tab's state while avoiding duplicate destinations.
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
                NavigationScreen(
                    onLogout = onLogout,
                    initialTargetNodeId = selectedTargetRoom?.nodeId,
                    targetRoomLabel = selectedTargetRoom?.roomNumber
                )
            }
            composable(BottomNavItem.Scheduling.route) {
                SchedulingScreen(
                    onLogout = onLogout,
                    onNavigateToBookedRoom = { room, _ ->
                        selectedTargetRoom = room
                        innerNavController.navigate(BottomNavItem.Navigation.route){
                            popUpTo(innerNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
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