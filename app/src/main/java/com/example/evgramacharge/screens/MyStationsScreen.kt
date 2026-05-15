package com.example.evgramacharge.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.evgramacharge.FirestoreStation
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStationsScreen(
    onBackClick: () -> Unit,
    onStationClick: (stationId: String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val vendorEmail = auth.currentUser?.email ?: ""
    var stations by remember { mutableStateOf(listOf<Pair<String, FirestoreStation>>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(vendorEmail) {
        if (vendorEmail.isNotEmpty()) {
            db.collection("stations")
                .whereEqualTo("vendorEmail", vendorEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MyStations", "Error fetching vendor stations", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        stations = snapshot.documents.mapNotNull { doc ->
                            try {
                                val station = FirestoreStation(
                                    name = doc.getString("name") ?: "Unknown Station",
                                    location = doc.getString("location") ?: "Unknown Location",
                                    socketType = doc.getString("socketType") ?: "Unknown",
                                    availability = doc.getString("availability") ?: "Available",
                                    latitude = doc.getDouble("latitude") ?: 0.0,
                                    longitude = doc.getDouble("longitude") ?: 0.0,
                                    vendorEmail = doc.getString("vendorEmail") ?: "",
                                    facilities = (doc.get("facilities") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    plugCount = doc.getLong("plugCount")?.toInt() ?: 1
                                )
                                Pair(doc.id, station)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Stations", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp).background(LightNavy.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkNavy)
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricGreen)
            }
        } else if (stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("You have not added any stations yet.", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(stations) { (id, station) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DeepBlue,
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onStationClick(id) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(station.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(station.location, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(station.socketType, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                Text("${station.plugCount ?: 1} plugs", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                Text(station.availability, color = if (station.availability == "Available") ElectricGreen else Color.Red, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}


