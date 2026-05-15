package com.example.evgramacharge.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    stationName: String,
    location: String,
    socketType: String,
    availability: String,
    latitude: Double,
    longitude: Double,
    facilities: List<String> = emptyList(),
    plugCount: Int = 1,
    onBookNowClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email
    
    var isFavorite by remember { mutableStateOf(false) }
    var isSavingFavorite by remember { mutableStateOf(false) }

    // Real-time Favorite Check
    LaunchedEffect(userEmail, stationName) {
        if (userEmail != null && stationName.isNotBlank()) {
            db.collection("favorites")
                .whereEqualTo("userEmail", userEmail)
                .whereEqualTo("name", stationName)
                .addSnapshotListener { snapshot, _ ->
                    isFavorite = snapshot != null && !snapshot.isEmpty
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Top Hero Section with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ElectricGreen.copy(alpha = 0.3f), DarkNavy)
                    )
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape,
                    color = ElectricGreen.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ElectricGreen.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 64.sp)
                    }
                }
            }

            // Navigation Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(DarkNavy.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(
                    onClick = {
                        if (userEmail == null || isSavingFavorite) return@IconButton
                        isSavingFavorite = true
                        if (isFavorite) {
                            db.collection("favorites")
                                .whereEqualTo("userEmail", userEmail)
                                .whereEqualTo("name", stationName)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        db.collection("favorites").document(document.id).delete()
                                    }
                                    isSavingFavorite = false
                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val favorite = hashMapOf(
                                "userEmail" to userEmail,
                                "name" to stationName,
                                "location" to location,
                                "socketType" to socketType,
                                "availability" to availability,
                                "latitude" to latitude,
                                "longitude" to longitude,
                                "facilities" to facilities,
                                "plugCount" to plugCount
                            )
                            db.collection("favorites").add(favorite).addOnSuccessListener {
                                isSavingFavorite = false
                                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                isSavingFavorite = false
                            }
                        }
                    },
                    modifier = Modifier.background(DarkNavy.copy(alpha = 0.5f), CircleShape)
                ) {
                    if (isSavingFavorite) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ElectricGreen, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }
        }

        // Main Content Card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 280.dp),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = DarkNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stationName.ifBlank { "Premium Charging Hub" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = " ${location.ifBlank { "Location details unavailable" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Surface(
                        color = ElectricGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Text(" 4.9", color = ElectricGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Feature Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailCard(label = "Charger Type", value = socketType.ifBlank { "Universal" }, icon = "🔌", modifier = Modifier.weight(1f))
                    DetailCard(label = "Status", value = availability, icon = "✅", modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailCard(
                    label = "Plug Availability", 
                    value = "$plugCount Active Points", 
                    icon = "⚡", 
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "About this Station",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "High-performance charging station located at ${location.ifBlank { "this prime spot" }}. Featuring reliable ${socketType.ifBlank { "standard" }} connectors. The facility is well-lit and maintained for a safe and efficient charging experience 24/7.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                // Amenities
                Text(
                    text = "On-Site Amenities",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (facilities.isEmpty()) {
                    Text(
                        "Essential charging services available.",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        facilities.forEach { facility ->
                            AmenityBadge(facility)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Primary Action
                Button(
                    onClick = onBookNowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricGreen,
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = availability.lowercase() != "busy",
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        if (availability.lowercase() == "busy") "Station Currently Busy" else "Proceed to Booking", 
                        color = DarkNavy, 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DetailCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = DeepBlue,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = ElectricGreen, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AmenityBadge(label: String) {
    val icon = when (label) {
        "Toilets", "Restrooms" -> "🚻"
        "Drinking Water" -> "💧"
        "Snacks & Drinks", "Cafe" -> "☕"
        "Resting Area", "Waiting Lounge" -> "🛋️"
        "WiFi" -> "📶"
        "Parking" -> "🅿️"
        "Air Pump" -> "🚲"
        "CCTV Security" -> "🛡️"
        "24/7 Availability" -> "🕒"
        else -> "✨"
    }

    Surface(
        color = LightNavy.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
        }
    }
}
