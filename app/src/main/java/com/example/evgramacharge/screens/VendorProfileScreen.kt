package com.example.evgramacharge.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
// com.example.evgramacharge.ui.theme.DeepBlue (not used in this screen)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(onBackClick: () -> Unit, onLogoutClick: () -> Unit, onStationsClick: () -> Unit, onBookingsClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val vendorEmail = auth.currentUser?.email ?: ""
    var totalStations by remember { mutableIntStateOf(0) }
    var totalBookings by remember { mutableIntStateOf(0) }

    LaunchedEffect(vendorEmail) {
        if (vendorEmail.isNotEmpty()) {
            db.collection("stations").whereEqualTo("vendorEmail", vendorEmail).addSnapshotListener { s, _ -> if (s != null) totalStations = s.size() }
            db.collection("bookings").whereEqualTo("vendorEmail", vendorEmail).addSnapshotListener { s, _ -> if (s != null) totalBookings = s.size() }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Vendor Profile", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold) }, navigationIcon = {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkNavy))
        },
        containerColor = DarkNavy
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = ElectricGreen.copy(alpha = 0.08f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(48.dp)) } }
            Text(auth.currentUser?.displayName ?: "Vendor", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(auth.currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsCard(title = "Stations", value = totalStations.toString(), icon = Icons.Default.EvStation, color = ElectricGreen, modifier = Modifier.weight(1f))
                AnalyticsCard(title = "Bookings", value = totalBookings.toString(), icon = Icons.Default.EventAvailable, color = Color(0xFF4FC3F7), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onStationsClick, modifier = Modifier.fillMaxWidth()) { Text("Manage Stations") }
            Button(onClick = onBookingsClick, modifier = Modifier.fillMaxWidth()) { Text("View Bookings") }
            Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.08f), contentColor = Color.Red)) { Text("Logout") }
        }
    }
}


