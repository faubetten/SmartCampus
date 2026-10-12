package com.smartcampus.client.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.screens.LoginScreen
import com.smartcampus.client.ui.screens.MainDashboardScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Automatically navigate into the app if a token is already present
    val startDestination = if (tokenManager.getToken() != null) {
        Screen.MainDashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MainDashboard.route) {
            MainDashboardScreen(
                onLogout = {
                    tokenManager.clearToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}