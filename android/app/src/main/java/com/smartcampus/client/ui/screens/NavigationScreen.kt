package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.CampusNodeDto
import com.smartcampus.client.ui.components.RouteNodesList
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen() {
    // This screen collects route parameters and renders the backend's path.
    // Keep one API client for the lifetime of this composable.
    val context = LocalContext.current
    val apiService = remember { NetworkClient.createService(context) }
    val coroutineScope = rememberCoroutineScope()

    var startId by remember { mutableStateOf("1") }
    var targetId by remember { mutableStateOf("3") }
    var accessibleOnly by remember { mutableStateOf(false) }
    // Compose state drives the loading indicator, errors, and displayed route.
    var routeNodes by remember { mutableStateOf<List<CampusNodeDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SmartCampus Wayfinding (A*)") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = startId,
                onValueChange = { startId = it },
                label = { Text("Start Node ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetId,
                onValueChange = { targetId = it },
                label = { Text("Target Node ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Accessible Route Only (No Stairs)")
                Switch(checked = accessibleOnly, onCheckedChange = { accessibleOnly = it })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        // Network work runs asynchronously so the UI remains responsive.
                        isLoading = true
                        errorMessage = null
                        try {
                            // Text input is converted only when the user requests a route.
                            val response = apiService.getRoute(
                                startId.toLong(),
                                targetId.toLong(),
                                accessibleOnly
                            )
                            if (response.isSuccessful) {
                                routeNodes = response.body() ?: emptyList()
                            } else {
                                errorMessage = "Error: ${response.code()} (Route not found)"
                                routeNodes = emptyList()
                            }
                        } catch (e: Exception) {
                            errorMessage = "Network Exception: ${e.localizedMessage}"
                            routeNodes = emptyList()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Optimal Route")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            // Each returned graph node is presented as one step of the route.
            RouteNodesList(
                routeNodes = routeNodes,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
