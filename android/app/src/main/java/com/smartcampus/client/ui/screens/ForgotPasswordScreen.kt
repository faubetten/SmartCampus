package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import com.smartcampus.client.data.model.ForgotPasswordRequest
import com.smartcampus.client.data.model.ResetPasswordRequest
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val apiService = remember (context, isPreview) { if(!isPreview) NetworkClient.createService(context) else null}

    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var step by remember { mutableIntStateOf(1) } // 1: Request Code, 2: Enter Code & New Password
    var receivedOtp by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Recovery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Login"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (step == 1) "Request Verification Code" else "Set New Password",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (step == 1)
                    "Enter your institutional email to generate a secure recovery code."
                else
                    "Enter the 6-digit code sent to your account along with your new password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (step == 1) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Institutional Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email address."
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = apiService?.forgotPassword(ForgotPasswordRequest(email.trim()))
                                if (response != null && response.isSuccessful && response.body() != null) {
                                    val body = response.body()!!
                                    receivedOtp = body.debugOtp
                                    step = 2
                                    successMessage = body.message
                                } else {
                                    val err = response?.errorBody()?.string() ?: "Failed to generate code."
                                    errorMessage = "Error (${response?.code()}): $err"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network failure: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Send Verification Code")
                    }
                }
            } else {
                // Live Demonstration OTP Notification Card
                receivedOtp?.let { code ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Code Available",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Demo Simulation Code",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your temporary verification code is: $code",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { otp = code },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Auto-fill OTP")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it; errorMessage = null },
                    label = { Text("6-Digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    label = { Text("New Password (min 8 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (otp.isBlank() || newPassword.isBlank()) {
                            errorMessage = "All fields are required."
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        if (newPassword.length < 8) {
                            errorMessage = "Password must be at least 8 characters."
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = apiService?.resetPassword(
                                    ResetPasswordRequest(
                                        email = email.trim(),
                                        otp = otp.trim(),
                                        newPassword = newPassword
                                    )
                                )
                                if (response != null && response.isSuccessful && response.body() != null) {
                                    onResetComplete()
                                } else {
                                    val err = response?.errorBody()?.string() ?: "Failed to reset password."
                                    errorMessage = "Error (${response?.code()}): $err"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network failure: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Confirm Password Reset")
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            successMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    SmartCampusClientTheme {
        ForgotPasswordScreen(
            onNavigateBack = {},
            onResetComplete = {}
        )
    }
}
