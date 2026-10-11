package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.LoginRequest
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch

@Composable
fun AuthTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val tokenManager = remember(context, isPreview) { if (!isPreview) TokenManager(context) else null }

    var email by remember { mutableStateOf("joao.silva@student.universidadeeuropeia.pt") }
    var password by remember { mutableStateOf("SecurePass123!") }
    var statusText by remember { mutableStateOf("Ready") }
    var hasToken by remember { mutableStateOf(tokenManager?.getToken() != null) }

    val apiService = remember(context, isPreview) {
        if (!isPreview) {
            NetworkClient.createService(
                context = context,
                onUnauthorized = {
                    hasToken = false
                    statusText = "Session expired (401). Redirected to login."
                }
            )
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SmartCampus Client", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    statusText = "Authenticating..."
                    try {
                        val response = apiService?.login(LoginRequest(email.trim(), password))
                        if (response != null && response.isSuccessful && response.body() != null) {
                            val token = response.body()!!.accessToken
                            tokenManager?.saveToken(token)
                            hasToken = true
                            statusText = "Authenticated! Token saved."
                        } else if (response != null) {
                            statusText = "Login failed: HTTP ${response.code()}"
                        } else {
                            statusText = "Preview mode: Authentication unavailable"
                        }
                    } catch (e: Exception) {
                        statusText = "Network Error: ${e.localizedMessage}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secured Request Test
        Button(
            onClick = {
                scope.launch {
                    statusText = "Requesting route..."
                    try {
                        val response = apiService?.getRoute(startId = 1, targetId = 5, accessibleOnly = false)
                        if (response != null && response.isSuccessful) {
                            val nodes = response.body() ?: emptyList()
                            statusText = "Route resolved! Nodes traversed: ${nodes.size}"
                        } else if (response != null) {
                            statusText = "Route error: HTTP ${response.code()}"
                        } else {
                            statusText = "Preview mode: Route endpoint unavailable"
                        }
                    } catch (e: Exception) {
                        statusText = "Route request failed: ${e.localizedMessage}"
                    }
                }
            },
            enabled = hasToken,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test A* Navigation Endpoint")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun AuthTestScreenPreview() {
    SmartCampusClientTheme {
        AuthTestScreen()
    }
}