package com.example.evgramacharge.screens

import com.example.evgramacharge.components.BottomNavBar
import com.example.evgramacharge.components.StatusChip
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.evgramacharge.FirestoreStation
import android.util.Log
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.DeepBlue
import com.google.firebase.firestore.Query

@Composable
fun EVHomeScreen(
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSeeAllStationsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onBatteryTipsClick: () -> Unit,
    onChargingHistoryClick: () -> Unit,
    onStationClick: (String, String, String, String, Double, Double, String, List<String>, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    val userName by remember { mutableStateOf(auth.currentUser?.displayName?.ifBlank { "Eco Rider" } ?: "Eco Rider") }
    var nearbyStations by remember { mutableStateOf(listOf<FirestoreStation>()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalBookingsCount by remember { mutableIntStateOf(0) }

    // Localization
    val greeting = "Welcome back"
    val subtitle = "Ready to power your journey?"
    val findStation = "Discover Stations"
    val energyTitle = "Energy Consumed"
    val bookingsTitle = "Total Sessions"
    val avgTimeTitle = "Avg. Charge Time"
    val carbonTitle = "Carbon Saved"

    // Real-time listener for user's total bookings and metrics
    LaunchedEffect(userEmail) {
        if (userEmail.isNotEmpty()) {
            db.collection("bookings")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        totalBookingsCount = snapshot.size()
                    }
                }
        }
    }

    // Real-time listener for nearby stations
    LaunchedEffect(Unit) {
        db.collection("stations")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3) // Show only 3 for cleaner preview
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeScreen", "Error fetching stations", error)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    nearbyStations = snapshot.documents.mapNotNull { doc ->
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
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
                isLoading = false
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onMapClick = onMapClick,
                onProfileClick = onProfileClick,
                selectedRoute = "home"
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(DeepBlue, DeepBlue.copy(alpha = 0.7f))),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Role",
                            tint = ElectricGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "$greeting, $userName!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Replace favorites heart with user notification access (two notification-style icons)
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = DeepBlue.copy(alpha = 0.6f),
                        onClick = onNotificationsClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ElectricGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = DeepBlue.copy(alpha = 0.6f),
                        onClick = onNotificationsClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Activity Center",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

                // Clean Home sections: My Bookings and Charging History
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your Overview",
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // My Bookings card (shows count)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = DeepBlue,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            onClick = onChargingHistoryClick
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(totalBookingsCount.toString(), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Text("My Bookings", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }

                        // Charging History quick access
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = DeepBlue,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            onClick = onChargingHistoryClick
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.History, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Charging History", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            // Enhanced Map Discovery Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(24.dp),
                color = DeepBlue,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ElectricGreen.copy(alpha = 0.08f),
                                        Color.Transparent,
                                        ElectricGreen.copy(alpha = 0.04f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = findStation,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Find optimal charging stations with real-time availability",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = ElectricGreen,
                            onClick = onMapClick
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    tint = DarkNavy,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nearby Stations Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Stations",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                TextButton(
                    onClick = onSeeAllStationsClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = ElectricGreen)
                ) {
                    Text(
                        "See More",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricGreen)
                }
            } else if (nearbyStations.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DeepBlue.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("No stations found nearby", color = Color.White.copy(alpha = 0.4f))
                    }
                }
            } else {
                nearbyStations.forEach { station ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
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
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = DarkNavy
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.EvStation,
                                            contentDescription = null,
                                            tint = ElectricGreen,
                                            modifier = Modifier.size(28.dp)
                                        )
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
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = " 4.8 • ${station.socketType}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StatusChip(
                                            text = station.availability,
                                            containerColor = if (station.availability == "Available") ElectricGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                            textColor = if (station.availability == "Available") ElectricGreen else Color.Red
                                        )
                                        StatusChip(
                                            text = "${station.plugCount ?: 1} Plugs",
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            textColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Battery Tips",
                    subtitle = "Optimize charging",
                    icon = Icons.Default.BatteryStd,
                    gradient = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                    modifier = Modifier.weight(1f),
                    onClick = onBatteryTipsClick
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PremiumMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DeepBlue,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = gradient.map { it.copy(alpha = 0.1f) }
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = gradient.first().copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = gradient.first(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = DeepBlue,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = gradient.map { it.copy(alpha = 0.08f) }
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = gradient.first().copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = gradient.first(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
