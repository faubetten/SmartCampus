package com.smartcampus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.model.CSPResultDto
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val apiService = remember(context, isPreview) {
        if (!isPreview) NetworkClient.createService(context) else null
    }
    val tokenManager = remember(context, isPreview) {
        if (!isPreview) TokenManager(context) else null
    }

    var capacityText by remember { mutableStateOf("4") }
    val roomTypes = listOf("STUDY_POD", "LABORATORY", "AUDITORIUM", "CLASSROOM")
    var selectedRoomType by remember { mutableStateOf(roomTypes[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Default to the next nearest hour slot
    val now = remember { LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1) }
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    var startTimeIso by remember { mutableStateOf(now.format(isoFormatter)) }
    var endTimeIso by remember { mutableStateOf(now.plusHours(2).format(isoFormatter)) }

    var isLoading by remember { mutableStateOf(false) }
    var allocationResult by remember { mutableStateOf<CSPResultDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Allocation (CSP)") },
                actions = {
                    IconButton(onClick = {
                        tokenManager?.clearToken()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log Out"
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Autonomous Resource Scheduler",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Backtracking CSP checks room capacity, type, and temporal conflicts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = capacityText,
                onValueChange = { capacityText = it },
                label = { Text("Required Group Capacity") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedRoomType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Facility Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    roomTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedRoomType = type
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = startTimeIso,
                onValueChange = { startTimeIso = it },
                label = { Text("Start Time (ISO-8601)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = endTimeIso,
                onValueChange = { endTimeIso = it },
                label = { Text("End Time (ISO-8601)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull()
                    if (cap == null || cap <= 0) {
                        errorMessage = "Capacity must be a valid positive integer."
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        allocationResult = null
                        try {
                            val response = apiService?.allocateRoom(
                                capacity = cap,
                                roomType = selectedRoomType,
                                startTime = startTimeIso,
                                endTime = endTimeIso
                            )
                            if (response != null && response.isSuccessful && response.body() != null) {
                                allocationResult = response.body()
                            } else if (response != null) {
                                errorMessage = "Allocation failed: HTTP ${response.code()} - ${response.errorBody()?.string()}"
                            } else {
                                errorMessage = "Network client unavailable in preview."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Network error: ${e.localizedMessage ?: "Server unreachable"}"
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
                    Text("Solve CSP & Allocate Facility")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            allocationResult?.let { res ->
                val containerColor = if (res.success) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Status",
                                tint = if (res.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (res.success) "Optimal Room Assigned" else "Allocation Failed",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = res.message, style = MaterialTheme.typography.bodyMedium)

                        res.assignedRoom?.let { room ->
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Room Number: ${room.roomNumber}", style = MaterialTheme.typography.titleMedium)
                            Text("Maximum Capacity: ${room.capacity} seats", style = MaterialTheme.typography.bodyMedium)
                            Text("Classification: ${room.roomType}", style = MaterialTheme.typography.bodyMedium)
                            Text("Connected Node ID: ${room.nodeId}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulingScreenPreview() {
    SmartCampusClientTheme {
        SchedulingScreen(
            onLogout = {}
        )
    }
}