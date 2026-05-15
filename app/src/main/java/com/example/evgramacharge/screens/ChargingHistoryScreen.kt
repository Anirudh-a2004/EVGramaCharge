package com.example.evgramacharge.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class BookingRecord(
    val stationName: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val timestamp: Long = 0,
    val userEmail: String = "",
    val status: String = "confirmed",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val startTimeMillis: Long = 0,
    val endTimeMillis: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingHistoryScreen(onBackClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var bookings by remember { mutableStateOf(listOf<BookingRecord>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Real-time listener for user bookings
    DisposableEffect(Unit) {
        val email = auth.currentUser?.email
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        
        if (email != null) {
            listener = db.collection("bookings")
                .whereEqualTo("userEmail", email)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        bookings = snapshot.toObjects(BookingRecord::class.java)
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
        
        onDispose {
            listener?.remove()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
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
            } else if (bookings.isEmpty()) {
                EmptyHistoryView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
                ) {
                    items(bookings) { booking ->
                        HistoryItem(booking)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(booking: BookingRecord) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString = sdf.format(Date(booking.timestamp))
    
    val currentTime = System.currentTimeMillis()
    val displayStatus = when {
        currentTime < booking.startTimeMillis -> "Upcoming"
        currentTime in booking.startTimeMillis..booking.endTimeMillis -> "Active"
        else -> "Completed"
    }
    
    val statusColor = when (displayStatus) {
        "Upcoming" -> Color(0xFF4FC3F7)
        "Active" -> ElectricGreen
        "Completed" -> Color.White.copy(alpha = 0.5f)
        else -> ElectricGreen
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = DeepBlue,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.stationName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "ID: ${booking.timestamp.toString().takeLast(6)}",
                        color = Color.White.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = displayStatus,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(LightNavy.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("🕒", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Slot Time", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
                    Text(text = "${booking.bookingDate} at ${booking.bookingTime}", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(LightNavy.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("📅", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Booked on", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
                    Text(text = dateString, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            // Navigate button only for confirmed bookings (Upcoming or Active)
            if (booking.status == "confirmed" && booking.latitude != 0.0 && displayStatus != "Completed") {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        try {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${booking.latitude},${booking.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${booking.latitude},${booking.longitude}"))
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = DarkNavy)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Navigate to Station", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView() {
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
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.1f))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Bookings Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "When you book a charging slot, it will appear here for easy access and navigation.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
