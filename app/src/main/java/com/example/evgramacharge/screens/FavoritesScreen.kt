package com.example.evgramacharge.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.FirestoreStation
import com.example.evgramacharge.components.StatusChip
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onStationClick: (String, String, String, String, Double, Double, String, List<String>, Int) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email
    
    var favoriteStations by remember { mutableStateOf(listOf<FirestoreStation>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Real-time listener for favorites
    DisposableEffect(userEmail) {
        val listener = if (userEmail != null) {
            db.collection("favorites")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("FavoritesScreen", "Error fetching favorites", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    
                    if (value != null) {
                        favoriteStations = value.documents.mapNotNull { doc ->
                            try {
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
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
            null
        }
        
        onDispose {
            listener?.remove()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Favorites", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
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
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricGreen)
                }
            } else if (favoriteStations.isEmpty()) {
                EmptyFavoritesView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
                ) {
                    items(favoriteStations) { station ->
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
                                
                                Icon(
                                    Icons.Default.Favorite, 
                                    contentDescription = "Favorite", 
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = DeepBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Red.copy(alpha = 0.1f))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your Favorites is Empty",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Save stations you visit often to find them quickly and check their real-time availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
