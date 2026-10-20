package com.smartcampus.client.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.screens.ForgotPasswordScreen
import com.smartcampus.client.ui.screens.LoginScreen
import com.smartcampus.client.ui.screens.MainDashboardScreen
import com.smartcampus.client.ui.screens.RegisterScreen

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
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route){
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
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

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )

        }

        composable(Screen.ForgotPassword.route) {  
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResetComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }
    }
}