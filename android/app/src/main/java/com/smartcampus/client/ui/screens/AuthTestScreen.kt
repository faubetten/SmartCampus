package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.LoginRequest
import com.smartcampus.client.security.TokenManager
import kotlinx.coroutines.launch

@Composable
fun AuthTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { NetworkClient.createService(context) }
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf("joao.silva@student.universidadeeuropeia.pt") }
    var password by remember { mutableStateOf("SecurePass123!") }
    var statusText by remember { mutableStateOf("Ready") }
    var hasToken by remember { mutableStateOf(tokenManager.getToken() != null) }

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
                        val response = apiService.login(LoginRequest(email.trim(), password))
                        if (response.isSuccessful && response.body() != null) {
                            val token = response.body()!!.accessToken
                            tokenManager.saveToken(token)
                            hasToken = true
                            statusText = "Authenticated! Token saved."
                        } else {
                            statusText = "Login failed: HTTP ${response.code()}"
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
                        val response = apiService.getRoute(startId = 1, targetId = 5, accessibleOnly = false)
                        if (response.isSuccessful) {
                            val nodes = response.body() ?: emptyList()
                            statusText = "Route resolved! Nodes traversed: ${nodes.size}"
                        } else {
                            statusText = "Route error: HTTP ${response.code()}"
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