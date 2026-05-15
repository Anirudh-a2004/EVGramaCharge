package com.example.evgramacharge.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorBookingsScreen(onBackClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val vendorEmail = auth.currentUser?.email ?: ""
    var bookings by remember { mutableStateOf(listOf<com.example.evgramacharge.screens.VendorBooking>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(vendorEmail) {
        if (vendorEmail.isNotEmpty()) {
            db.collection("bookings")
                .whereEqualTo("vendorEmail", vendorEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        bookings = snapshot.toObjects(com.example.evgramacharge.screens.VendorBooking::class.java)
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
                title = { Text("Station Bookings", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        } else if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No bookings yet", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bookings) { booking ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DeepBlue, border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = DarkNavy) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricGreen) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.stationName, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text(booking.userEmail, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                                Text(booking.bookingDate + " " + booking.bookingTime, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(booking.status.replaceFirstChar { it.titlecase() }, color = if (booking.status == "completed") Color.White.copy(alpha = 0.5f) else ElectricGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

