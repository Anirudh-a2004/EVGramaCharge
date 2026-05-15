package com.example.evgramacharge.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.FirestoreStation
import com.example.evgramacharge.components.StatusChip
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStationsScreen(
    onBackClick: () -> Unit,
    onStationClick: (String, String, String, String, Double, Double, String, List<String>, Int) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var stations by remember { mutableStateOf(listOf<FirestoreStation>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val filteredStations = remember(searchQuery, stations) {
        if (searchQuery.isBlank()) stations
        else stations.filter { it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        db.collection("stations")
            .get()
            .addOnSuccessListener { result ->
                val parsedStations = mutableListOf<FirestoreStation>()
                for (doc in result.documents) {
                    try {
                        parsedStations.add(
                            FirestoreStation(
                                name = doc.getString("name") ?: "Unknown Station",
                                location = doc.getString("location") ?: "Unknown Location",
                                socketType = doc.getString("socketType") ?: "Unknown",
                                availability = doc.getString("availability") ?: "Available",
                                latitude = doc.getDouble("latitude") ?: 0.0,
                                longitude = doc.getDouble("longitude") ?: 0.0,
                                vendorEmail = doc.getString("vendorEmail") ?: "unknown@example.com",
                                facilities = (doc.get("facilities") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                plugCount = doc.getLong("plugCount")?.toInt() ?: 1
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("AllStations", "Error parsing station: ${e.message}")
                    }
                }
                stations = parsedStations
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkNavy)) {
                CenterAlignedTopAppBar(
                    title = { Text("Search Stations", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
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
                
                // Professional Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Type station name or city...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricGreen) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = LightNavy.copy(alpha = 0.2f),
                        unfocusedContainerColor = LightNavy.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricGreen)
            }
        } else if (filteredStations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No stations match your search", color = Color.White.copy(alpha = 0.4f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredStations) { station ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = DeepBlue,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        onClick = { 
                            onStationClick(
                                station.name, station.location, station.socketType, station.availability, 
                                station.latitude, station.longitude, station.vendorEmail, station.facilities ?: emptyList(), station.plugCount ?: 1
                            ) 
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = DarkNavy
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("⚡", fontSize = 28.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = " 4.8 • ${station.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusChip(text = station.socketType, containerColor = LightNavy.copy(alpha = 0.5f))
                                    StatusChip(
                                        text = "${station.plugCount ?: 1} Plugs",
                                        containerColor = ElectricGreen.copy(alpha = 0.1f), 
                                        textColor = ElectricGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
