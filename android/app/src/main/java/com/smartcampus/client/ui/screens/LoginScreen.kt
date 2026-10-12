package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.LoginRequest
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val tokenManager =
        remember(context, isPreview) { if (!isPreview) TokenManager(context) else null }

    var email by remember { mutableStateOf("joao.silva@student.universidadeeuropeia.pt") }
    var password by remember { mutableStateOf("SecurePass123!") }
    var hasToken by remember { mutableStateOf(tokenManager?.getToken() != null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val apiService = remember(context, isPreview) {
        if (!isPreview) {
            NetworkClient.createService(
                context = context,
                onUnauthorized = {
                    hasToken = false
                    errorMessage = "Token has expired"
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

        Text(
            text = "Autonomous Campus Navigation & Scheduling",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password cannot be empty."
                    return@Button
                }

                scope.launch {
                    errorMessage = null
                    isLoading = true
                    try {
                        val response = apiService?.login(LoginRequest(email.trim(), password))
                        if (response != null && response.isSuccessful && response.body() != null) {
                            val token = response.body()!!.accessToken
                            tokenManager?.saveToken(token)
                            hasToken = true
                            onLoginSuccess()
                        } else if (response != null) {
                            errorMessage =
                                "Authentication failed (HTTP ${response.code()}). Check credentials."
                        } else {
                            errorMessage = "Preview mode: Authentication endpoint unavailable"
                        }
                    } catch (e: Exception) {
                        errorMessage =
                            "Connection error: ${e.localizedMessage ?: "Unable to connect."}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmartCampusClientTheme {
        LoginScreen()
    }
}