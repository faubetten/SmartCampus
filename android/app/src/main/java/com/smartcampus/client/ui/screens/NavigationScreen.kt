package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.CampusNodeDto
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.components.RouteNodesList
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    onLogout: () -> Unit = {}
) {
    // This screen collects route parameters and renders the backend's path.
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val apiService = remember(context, isPreview) {
        if (!isPreview) NetworkClient.createService(context) else null
    }
    val scope = rememberCoroutineScope()
    val tokenManager = remember(context, isPreview) {
        if (!isPreview) TokenManager(context) else null
    }

    var startId by remember { mutableStateOf("1") }
    var targetId by remember { mutableStateOf("3") }
    var accessibleOnly by remember { mutableStateOf(false) }
    // Compose state drives the loading indicator, errors, and displayed route.
    var routeNodes by remember { mutableStateOf<List<CampusNodeDto>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartCampus Navigation (A*)") },
                actions = {
                    IconButton(onClick = {
                        tokenManager?.clearToken()
                        onLogout()
                    }){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startId,
                    onValueChange = { startId = it },
                    label = { Text("Start Node ID") },
                    modifier = Modifier.weight(1f),
                    singleLine = true

                )
                OutlinedTextField(
                    value = targetId,
                    onValueChange = { targetId = it },
                    label = { Text("Target Node ID") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text( text = "Accessible Route Only (No Stairs)", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = accessibleOnly, onCheckedChange = { accessibleOnly = it })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Input validation
                    val sId = startId.toLongOrNull()
                    val tId = targetId.toLongOrNull()
                    if (sId == null || tId == null) {
                        statusMessage = "Invalid Node IDs"
                        return@Button
                    }
                    scope.launch {
                        // Network work runs asynchronously so the UI remains responsive.
                        isLoading = true
                        statusMessage = "Calculating optimal route..."
                        try {
                            // Text input is converted only when the user requests a route.
                            val response = apiService?.getRoute(
                                startId = sId,
                                targetId = tId,
                                accessibleOnly = accessibleOnly
                            )
                            if (response?.body() != null && response.isSuccessful) {
                                routeNodes = response.body()!!
                                statusMessage = "Route calculated successfully!"
                            } else if (response != null) {
                                statusMessage = "Error: ${response.code()} (Route not found)"
                                routeNodes = emptyList()
                            } else {
                                statusMessage = "Preview mode: Route service unavailable"
                                routeNodes = emptyList()
                            }
                        } catch (e: Exception) {
                            statusMessage = "Navigation failed: ${e.localizedMessage}"
                            routeNodes = emptyList()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                else{
                Text("Calculate Optimal Route")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            statusMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Each returned graph node is presented as one step of the route.

            if (routeNodes.isNotEmpty()) {
                RouteNodesList(
                    routeNodes = routeNodes,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationScreenPreview() {
    SmartCampusClientTheme {
        NavigationScreen()
    }
}

