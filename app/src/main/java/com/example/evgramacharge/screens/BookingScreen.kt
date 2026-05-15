@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.evgramacharge.screens

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingScreen(
    stationName: String,
    vendorEmail: String,
    totalPlugs: Int,
    latitude: Double,
    longitude: Double,
    onBackClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email ?: "anonymous@evgrama.com"

    var bookingDone by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Date and Time State
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedDuration by remember { mutableIntStateOf(30) } // Default 30 mins

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val formattedDate = dateFormatter.format(Date(selectedDate))
    val calendarTime = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        set(Calendar.HOUR_OF_DAY, selectedHour)
        set(Calendar.MINUTE, selectedMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startTimeMillis = calendarTime.timeInMillis
    val formattedTime = timeFormatter.format(calendarTime.time)
    
    val endTimeMillis = startTimeMillis + (selectedDuration * 60 * 1000)
    val formattedEndTime = timeFormatter.format(Date(endTimeMillis))

    // Real-time Availability logic
    var occupiedPlugs by remember { mutableIntStateOf(0) }
    var availabilityStatus by remember { mutableStateOf("Checking...") }

    // Real-time listener for current slot availability
    DisposableEffect(stationName, formattedDate, startTimeMillis, endTimeMillis) {
        val listener: ListenerRegistration = db.collection("bookings")
            .whereEqualTo("stationName", stationName)
            .whereEqualTo("bookingDate", formattedDate)
            .whereEqualTo("status", "confirmed")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    availabilityStatus = "Error checking"
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    var count = 0
                    for (doc in snapshot.documents) {
                        val bStart = doc.getLong("startTimeMillis") ?: 0L
                        val bEnd = doc.getLong("endTimeMillis") ?: 0L
                        
                        // Overlap check: (StartA < EndB) and (EndA > StartB)
                        if (startTimeMillis < bEnd && endTimeMillis > bStart) {
                            count++
                        }
                    }
                    occupiedPlugs = count
                    availabilityStatus = when {
                        occupiedPlugs >= totalPlugs -> "Fully Occupied"
                        totalPlugs - occupiedPlugs <= (totalPlugs / 4).coerceAtLeast(1) -> "Few Plugs Left"
                        else -> "Available"
                    }
                }
            }
        
        onDispose {
            listener.remove()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Book a Slot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp).background(LightNavy.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (bookingDone) {
                BookingSuccessView(stationName, formattedDate, formattedTime, onBackClick)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StationInfoCard(stationName)

                    Spacer(modifier = Modifier.height(24.dp))

                    AvailabilityIndicator(availabilityStatus, (totalPlugs - occupiedPlugs).coerceAtLeast(0))

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(text = "Schedule Charging", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "Select your preferred date, time and duration", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(24.dp))

                    BookingSelectorItem(label = "Select Date", value = formattedDate, icon = Icons.Default.DateRange, onClick = { showDatePicker = true })
                    Spacer(modifier = Modifier.height(16.dp))
                    BookingSelectorItem(label = "Start Time", value = formattedTime, icon = Icons.Default.AccessTime, onClick = { showTimePicker = true })

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Charging Duration", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DurationSelection(selectedDuration) { selectedDuration = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Session ends at: $formattedEndTime", style = MaterialTheme.typography.bodySmall, color = ElectricGreen.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            // Validate selected start time is in the future
                            if (startTimeMillis <= System.currentTimeMillis()) {
                                errorMessage = "Please select a future date and time for booking."
                                return@Button
                            }

                            if (occupiedPlugs >= totalPlugs) {
                                errorMessage = "This slot is no longer available. Please select another time."
                                return@Button
                            }
                            isLoading = true
                            val timestamp = System.currentTimeMillis()
                            val booking = hashMapOf(
                                "userEmail" to currentUserEmail,
                                "stationName" to stationName,
                                "vendorEmail" to vendorEmail,
                                "bookingDate" to formattedDate,
                                "bookingTime" to formattedTime,
                                "startTimeMillis" to startTimeMillis,
                                "endTimeMillis" to endTimeMillis,
                                "duration" to selectedDuration,
                                "latitude" to latitude,
                                "longitude" to longitude,
                                "timestamp" to timestamp,
                                "status" to "confirmed"
                            )
                            db.collection("bookings")
                                .add(booking)
                                .addOnSuccessListener {
                                    sendNotifications(db, currentUserEmail, vendorEmail, stationName, formattedDate, formattedTime, timestamp)
                                    isLoading = false
                                    bookingDone = true
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Booking failed: ${e.localizedMessage}"
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (occupiedPlugs >= totalPlugs || startTimeMillis <= System.currentTimeMillis()) Color.Gray else ElectricGreen,
                            disabledContainerColor = if (occupiedPlugs >= totalPlugs || startTimeMillis <= System.currentTimeMillis()) Color.Gray.copy(alpha = 0.5f) else ElectricGreen.copy(alpha = 0.5f)
                        ),
                        enabled = !isLoading && occupiedPlugs < totalPlugs && startTimeMillis > System.currentTimeMillis(),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = DarkNavy, strokeWidth = 3.dp)
                        } else {
                            Text(
                                text = when {
                                    startTimeMillis <= System.currentTimeMillis() -> "Invalid Time"
                                    occupiedPlugs >= totalPlugs -> "Fully Occupied"
                                    else -> "Confirm Booking"
                                },
                                color = DarkNavy,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

            // Material 3 Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val todayStart = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        return utcTimeMillis >= todayStart
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                                val picked = datePickerState.selectedDateMillis
                                if (picked != null) {
                                    // Prevent selecting past dates (before today start)
                                    val todayStart = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis

                                    if (picked < todayStart) {
                                        errorMessage = "Please select a present or future date"
                                    } else {
                                        selectedDate = picked
                                        // If selected date is today, ensure time is not in the past by adjusting hour/minute if needed
                                        val now = System.currentTimeMillis()
                                        val calPicked = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                        if (calPicked.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                                            && calPicked.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                                            // if picked today and current selected time is already past, advance to next valid hour
                                            val calTime = Calendar.getInstance().apply {
                                                timeInMillis = selectedDate
                                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                                set(Calendar.MINUTE, selectedMinute)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            if (calTime.timeInMillis <= now) {
                                                // set start to next full hour
                                                val next = Calendar.getInstance().apply {
                                                    timeInMillis = now + 60 * 60 * 1000L
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                selectedHour = next.get(Calendar.HOUR_OF_DAY)
                                                selectedMinute = next.get(Calendar.MINUTE)
                                            }
                                        }
                                        showDatePicker = false
                                    }
                                } else {
                                    showDatePicker = false
                                }
                    }) { Text("OK", color = ElectricGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.White) }
                },
                colors = DatePickerDefaults.colors(containerColor = DeepBlue)
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Material 3 Time Picker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val pickedHour = timePickerState.hour
                        val pickedMinute = timePickerState.minute

                        // Build candidate startTime using selectedDate and picked time
                        val calCandidate = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, pickedHour)
                            set(Calendar.MINUTE, pickedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val now = System.currentTimeMillis()
                        // If candidate is in the past, prevent selection for today's date
                        val calSelectedDate = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        val today = Calendar.getInstance()
                        val isToday = calSelectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calSelectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                        if (calCandidate.timeInMillis <= now && isToday) {
                            errorMessage = "Please select a future time for today"
                        } else {
                            selectedHour = pickedHour
                            selectedMinute = pickedMinute
                            showTimePicker = false
                        }
                    }) { Text("OK", color = ElectricGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = Color.White) }
                },
                containerColor = DeepBlue,
                text = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select Start Time", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                        TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(
                            clockDialColor = LightNavy,
                            selectorColor = ElectricGreen,
                            containerColor = DeepBlue
                        )) 
                    }
                }
            )
        }
    }
}

@Composable
fun DurationSelection(selectedDuration: Int, onDurationSelected: (Int) -> Unit) {
    val durations = listOf(10, 20, 30, 45, 60, 120)
    val labels = listOf("10m", "20m", "30m", "45m", "1h", "2h")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightNavy.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        durations.zip(labels).forEach { (mins, label) ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .clickable { onDurationSelected(mins) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedDuration == mins) ElectricGreen else Color.Transparent
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedDuration == mins) DarkNavy else Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun AvailabilityIndicator(status: String, availableCount: Int) {
    val color = when (status) {
        "Available" -> ElectricGreen
        "Few Plugs Left" -> Color(0xFFFFC107)
        "Fully Occupied" -> Color(0xFFFF5252)
        else -> Color.White.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = status, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                if (status != "Fully Occupied" && status != "Checking..." && status != "Error checking") {
                    Text(text = "$availableCount plugs currently available for this slot", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BookingSelectorItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = LightNavy.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricGreen.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun StationInfoCard(stationName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = DeepBlue,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(12.dp), color = DarkNavy) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⛽", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Charging Station", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(stationName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun BookingSuccessView(stationName: String, date: String, time: String, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = ElectricGreen.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(72.dp), tint = ElectricGreen)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your slot at $stationName on $date at $time is secured.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Back to Map", color = DarkNavy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

fun sendNotifications(db: FirebaseFirestore, userEmail: String, vendorEmail: String, stationName: String, date: String, time: String, ts: Long) {
    val userNotif = hashMapOf(
        "userEmail" to userEmail,
        "title" to "Booking Confirmed",
        "message" to "Confirmed for $stationName on $date at $time.",
        "timestamp" to ts,
        "type" to "booking",
        "isVendor" to false
    )
    db.collection("notifications").add(userNotif)
    
    val vendorNotif = hashMapOf(
        "userEmail" to vendorEmail,
        "title" to "New Booking",
        "message" to "$userEmail booked $stationName for $date at $time.",
        "timestamp" to ts,
        "type" to "booking",
        "isVendor" to true
    )
    db.collection("notifications").add(vendorNotif)
}
