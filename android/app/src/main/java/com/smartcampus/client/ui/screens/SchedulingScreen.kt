package com.smartcampus.client.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.api.NetworkClient
import com.smartcampus.client.data.dto.RoomDetailsDto
import com.smartcampus.client.data.responseDto.BookingResponseDto
import com.smartcampus.client.security.TokenManager
import com.smartcampus.client.ui.theme.SmartCampusClientTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


enum class BookingStep {
    SEARCH,
    DETAILS,
    CONFIRMED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingScreen(
    onLogout: () -> Unit,
    onNavigateToBookedRoom: (RoomDetailsDto, Long) -> Unit = {_,_ ->}
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


    //Search parameters
    var capacityText by remember { mutableStateOf("4") }
    val roomTypes = listOf("ANY","STUDY_POD", "LABORATORY", "AUDITORIUM", "CLASSROOM")
    var selectedRoomType by remember { mutableStateOf(roomTypes[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Default to the next nearest hour slot
    val now = remember { LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1).withHour(10) }
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    var startTimeIso by remember { mutableStateOf(now.format(isoFormatter)) }
    var endTimeIso by remember { mutableStateOf(now.plusHours(2).format(isoFormatter)) }

    var currentStep by remember { mutableStateOf(BookingStep.SEARCH) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var availableRooms by remember { mutableStateOf<List<RoomDetailsDto>?>(null) }
    var selectedRoom by remember { mutableStateOf<RoomDetailsDto?>(null) }
    var confirmedBooking by remember { mutableStateOf<BookingResponseDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when {
                        confirmedBooking != null -> Text("Booking Confirmed")
                        selectedRoom != null -> Text("Room ${selectedRoom?.roomNumber ?: ""} Details")
                        else -> Text("Room Search")
                    } },


                navigationIcon = {
                    if (selectedRoom!= null && confirmedBooking == null) {
                        IconButton(onClick = {selectedRoom = null; errorMessage = null; hasSearched = false; currentStep = BookingStep.SEARCH}){
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")

                        }
                    }
                },
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


            // ==========================================
            // RESERVATION CONFIRMED
            // ==========================================
            when (currentStep) {
                BookingStep.CONFIRMED -> {
                    val booking = confirmedBooking
                    val room = booking?.roomDetails ?: selectedRoom


                    if (room != null) {//TODO: make into a component
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Confirmed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Reservation #${booking?.reservationId} Confirmed",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Status: ${booking?.status}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Facility: Room ${room.roomNumber} (${room.roomType})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Building: ${room.buildingName ?: "Main Building"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Floor: Floor ${room.floorNumber ?: 0} (Map Node #${room.nodeId})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Capacity: ${room.capacity} seats",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Start: ${booking?.startTime}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "End: ${booking?.endTime}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        onNavigateToBookedRoom(room, booking?.reservationId ?: 0L)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Visualize Building & Start Navigation")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        confirmedBooking = null
                                        selectedRoom = null
                                        hasSearched = false
                                        currentStep = BookingStep.SEARCH
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Book Another Room")
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // ROOM DETAILS & CONFIRMATION
                // ==========================================
                BookingStep.DETAILS -> {
                    val room = selectedRoom


                    if (room != null) {//TODO: make into a component
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Room ${room.roomNumber}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(room.roomType) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = room.buildingName ?: "Campus Building",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Location: Floor ${room.floorNumber ?: 0} • Graph Node #${room.nodeId}")
                                Text("Coordinates: (${room.coordX ?: 0.0}m, ${room.coordY ?: 0.0}m)")
                                Text("Maximum Capacity: ${room.capacity} people")

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Installed Equipment",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (room.equipment.isEmpty()) {
                                    Text(
                                        "Standard classroom furniture (No special equipment)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    room.equipment.forEach { item ->
                                        Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Selected Time Window",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                //TODO: Better time format
                                Text("From: $startTimeIso", style = MaterialTheme.typography.bodySmall)
                                Text("To:   $endTimeIso", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            try {
                                                val response = apiService?.bookSpecificRoom(
                                                    roomId = room.roomId,
                                                    startTime = startTimeIso,
                                                    endTime = endTimeIso
                                                )
                                                if (response != null && response.isSuccessful && response.body() != null) {
                                                    confirmedBooking = response.body()
                                                    currentStep = BookingStep.CONFIRMED
                                                    selectedRoom = null
                                                    hasSearched = false
                                                } else if (response != null) {
                                                    errorMessage = "Booking failed: ${
                                                        response.errorBody()
                                                            ?.string() ?: "HTTP ${response.code()}"
                                                    }"
                                                } else {
                                                    errorMessage = "Network client unavailable."
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Network error: ${e.localizedMessage}"
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
                                        Text("Schedule Reservation")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { selectedRoom = null; errorMessage = null; currentStep = BookingStep.SEARCH },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Back to Available Rooms")
                                }
                            }
                        }
                    }

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                    return@Column
                }

                // ==========================================
                // PREFERENCES & AVAILABLE ROOMS LIST
                // ==========================================
                BookingStep.SEARCH -> {
                    Text(
                        text = "1. Set Your Room Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = capacityText,
                            onValueChange = { capacityText = it },
                            label = { Text("Min Capacity") },
                            singleLine = true,
                            modifier = Modifier.weight(0.45f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it },
                            modifier = Modifier.weight(0.55f)
                        ) {
                            OutlinedTextField(
                                value = selectedRoomType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Room Type") },
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
                    }
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            //TODO: Better date picker

            OutlinedTextField(
                value = startTimeIso,
                onValueChange = { startTimeIso = it },
                label = { Text("Start Time (YYYY-MM-DDTHH:MM:SS)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = endTimeIso,
                onValueChange = { endTimeIso = it },
                label = { Text("End Time (YYYY-MM-DDTHH:MM:SS)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        hasSearched = true
                        try {
                            val response = apiService?.findAvailableRooms(
                                capacity = cap,
                                roomType = selectedRoomType,
                                startTime = startTimeIso,
                                endTime = endTimeIso
                            )
                            val body = response?.body()
                            if (response != null && response.isSuccessful && body != null) {
                                availableRooms = body
                            } else if (response != null) {
                                availableRooms = null
                                errorMessage = "Search failed: HTTP ${response.code()} - ${response.errorBody()?.string()}"
                            } else {
                                errorMessage = "Network client unavailable in preview."
                            }
                        } catch (e: Exception) {
                            availableRooms = null
                            errorMessage = "Network error: ${e.localizedMessage ?: "Server unreachable"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Find available rooms")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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

            if (hasSearched && !isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                val rooms = availableRooms
                Text(
                    text = "2. Select an Available Room (${rooms?.size ?: 0} found)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (rooms.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No rooms match your capacity/type preferences for this time slot. Try lowering capacity or selecting 'ANY'.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    rooms.forEach { room ->
                        //TODO: make into a component
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedRoom = room
                                    currentStep = BookingStep.DETAILS
                                    hasSearched = false
                                    errorMessage = null
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Room ${room.roomNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(
                                            text = room.roomType,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Floor ${room.floorNumber ?: 0} • Capacity: ${room.capacity} seats",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (room.equipment.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Equipment: ${room.equipment.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to view details & schedule →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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