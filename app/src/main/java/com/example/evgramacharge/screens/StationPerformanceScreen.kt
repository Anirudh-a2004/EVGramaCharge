package com.example.evgramacharge.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationPerformanceScreen(stationId: String, onBackClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var station by remember { mutableStateOf<com.example.evgramacharge.FirestoreStation?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var totalBookings by remember { mutableIntStateOf(0) }
    var activeBookings by remember { mutableIntStateOf(0) }
    var completedBookings by remember { mutableIntStateOf(0) }

    // editable fields
    var description by remember { mutableStateOf("") }
    var plugCount by remember { mutableIntStateOf(1) }
    var availability by remember { mutableStateOf("") }
    var facilitiesInput by remember { mutableStateOf("") }

    LaunchedEffect(stationId) {
        if (stationId.isNotEmpty()) {
            db.collection("stations").document(stationId)
                .addSnapshotListener { doc, err ->
                    if (err != null) {
                        Log.e("StationPerf", "error", err)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (doc != null && doc.exists()) {
                        station = doc.toObject(com.example.evgramacharge.FirestoreStation::class.java)
                        description = doc.getString("description") ?: station?.name ?: ""
                        plugCount = (doc.getLong("plugCount") ?: 1L).toInt()
                        availability = doc.getString("availability") ?: "Available"
                        facilitiesInput = ((doc.get("facilities") as? List<*>)?.filterIsInstance<String>()?.joinToString(", ")) ?: ""
                    } else {
                        station = null
                    }
                    isLoading = false
                }

            // Bookings aggregation (assumes bookings have stationId field)
            db.collection("bookings")
                .whereEqualTo("stationId", stationId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        totalBookings = snapshot.size()
                        val recs = snapshot.documents.mapNotNull { it.toObject(com.example.evgramacharge.screens.VendorBooking::class.java) }
                        activeBookings = recs.count { System.currentTimeMillis() in it.startTimeMillis..it.endTimeMillis }
                        completedBookings = recs.count { it.status == "completed" }
                    }
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(station?.name ?: "Station", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* open edit if needed */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ElectricGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkNavy)
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricGreen)
            }
        } else if (station == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Station not found", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Analytics Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsCard(title = "Total", value = totalBookings.toString(), icon = Icons.Default.EvStation, color = ElectricGreen, modifier = Modifier.weight(1f))
                    AnalyticsCard(title = "Active", value = activeBookings.toString(), icon = Icons.Default.EventAvailable, color = Color(0xFFFFD54F), modifier = Modifier.weight(1f))
                    AnalyticsCard(title = "Completed", value = completedBookings.toString(), icon = Icons.Default.History, color = Color(0xFF4FC3F7), modifier = Modifier.weight(1f))
                }

                // Details Card (editable)
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DeepBlue, border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Station Details", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricGreen))
                        OutlinedTextField(value = plugCount.toString(), onValueChange = { plugCount = it.toIntOrNull() ?: plugCount }, label = { Text("Plug Count") }, singleLine = true)
                        OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Availability") }, singleLine = true)
                        OutlinedTextField(value = facilitiesInput, onValueChange = { facilitiesInput = it }, label = { Text("Facilities (comma separated)") })
                        Button(onClick = {
                            // update Firestore
                            val updates = hashMapOf<String, Any>(
                                "description" to description,
                                "plugCount" to plugCount,
                                "availability" to availability,
                                "facilities" to facilitiesInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                            db.collection("stations").document(stationId).update(updates)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}


