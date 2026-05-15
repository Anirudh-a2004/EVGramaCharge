package com.example.evgramacharge.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

// Model for Vendor Bookings
data class VendorBooking(
    val stationName: String = "",
    val userEmail: String = "",
    val timestamp: Long = 0,
    val bookingDate: String = "",
    val bookingTime: String = "",
    val status: String = "confirmed",
    val startTimeMillis: Long = 0,
    val endTimeMillis: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    onAddStationClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onBackClick: () -> Unit,
    // New parameters for user features access
    onMapClick: (() -> Unit)? = null,
    onBookingsClick: (() -> Unit)? = null,
    onNearbyStationsClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email

    var totalStations by remember { mutableIntStateOf(0) }
    var totalBookings by remember { mutableIntStateOf(0) }
    var activeStations by remember { mutableIntStateOf(0) }
    var totalPlugs by remember { mutableIntStateOf(0) }
    var recentBookings by remember { mutableStateOf(listOf<VendorBooking>()) }
    var isLoading by remember { mutableStateOf(true) }

    val weeklyStats = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f) }

    DisposableEffect(userEmail) {
        var stationsListener: ListenerRegistration? = null
        var bookingsListener: ListenerRegistration? = null

        if (userEmail != null) {
            stationsListener = db.collection("stations")
                .whereEqualTo("vendorEmail", userEmail)
                .addSnapshotListener { result, _ ->
                    if (result != null) {
                        totalStations = result.size()
                        var plugsSum = 0
                        var activeCount = 0
                        result.documents.forEach { doc ->
                            plugsSum += (doc.getLong("plugCount") ?: 0L).toInt()
                            if (doc.getString("availability")?.lowercase() == "available") {
                                activeCount++
                            }
                        }
                        totalPlugs = plugsSum
                        activeStations = activeCount
                    }
                }

            bookingsListener = db.collection("bookings")
                .whereEqualTo("vendorEmail", userEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { bookingResult, _ ->
                    if (bookingResult != null) {
                        totalBookings = bookingResult.size()
                        recentBookings = bookingResult.toObjects(VendorBooking::class.java)

                        // Weekly Analytics Logic
                        val cal = Calendar.getInstance()
                        val counts = FloatArray(7)
                        val today = cal.get(Calendar.DAY_OF_YEAR)

                        bookingResult.documents.forEach { doc ->
                            val timeMillis = doc.getLong("timestamp") ?: 0L
                            if (timeMillis > 0) {
                                cal.timeInMillis = timeMillis
                                val bDay = cal.get(Calendar.DAY_OF_YEAR)
                                val diff = today - bDay
                                if (diff in 0..6) {
                                    counts[6 - diff] += 1f
                                }
                            }
                        }

                        val max = counts.maxOrNull() ?: 1f
                        counts.forEachIndexed { i, f ->
                            weeklyStats[i] = if (max > 0) f / max else 0f
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }

        onDispose {
            stationsListener?.remove()
            bookingsListener?.remove()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Vendor Hub", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp).background(LightNavy.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.padding(end = 8.dp).background(LightNavy.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ElectricGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkNavy,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStationClick,
                containerColor = ElectricGreen,
                contentColor = DarkNavy,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Station", fontWeight = FontWeight.Bold) },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Vendor Quick Access — focused vendor shortcuts only
                if (onBookingsClick != null || onNearbyStationsClick != null || onProfileClick != null) {
                    Text(
                        text = "Quick Access",
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricGreen,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Replace All Stations with My Stations (vendor-owned)
                        onNearbyStationsClick?.let {
                            QuickAccessCard(
                                title = "My Stations",
                                subtitle = "Manage your locations",
                                icon = Icons.Default.EvStation,
                                gradient = listOf(Color(0xFF00E676), Color(0xFF00C853)),
                                modifier = Modifier.weight(1f),
                                onClick = it
                            )
                        }
                        // Keep profile shortcut
                        onProfileClick?.let {
                            QuickAccessCard(
                                title = "My Profile",
                                subtitle = "Vendor settings",
                                icon = Icons.Default.Person,
                                gradient = listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)),
                                modifier = Modifier.weight(1f),
                                onClick = it
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Rename My Bookings to Station Bookings
                        onBookingsClick?.let {
                            QuickAccessCard(
                                title = "Station Bookings",
                                subtitle = "Bookings for your stations",
                                icon = Icons.Default.EventAvailable,
                                gradient = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                                modifier = Modifier.weight(1f),
                                onClick = it
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) { /* layout balance */ }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(
                    text = "Performance Overview",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsCard(
                        title = "Stations",
                        value = totalStations.toString(),
                        icon = Icons.Default.EvStation,
                        color = ElectricGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Bookings",
                        value = totalBookings.toString(),
                        icon = Icons.Default.EventAvailable,
                        color = Color(0xFF4FC3F7),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepBlue,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusSection(label = "Active", value = activeStations, color = ElectricGreen)
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.White.copy(alpha = 0.1f))
                        StatusSection(label = "Total Plugs", value = totalPlugs, color = Color(0xFFFFD54F))
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.White.copy(alpha = 0.1f))
                        // Remove Earnings (app has no payments). Show Completed bookings instead.
                        StatusSection(label = "Completed", value = recentBookings.count { it.status == "completed" }, color = Color(0xFF4FC3F7))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Booking Trends",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DeepBlue,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        weeklyStats.forEachIndexed { index, heightFactor ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(heightFactor.coerceAtLeast(0.05f))
                                        .background(
                                            Brush.verticalGradient(listOf(ElectricGreen, ElectricGreen.copy(alpha = 0.3f))),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(days[index], color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (recentBookings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp).background(DeepBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent bookings found", color = Color.White.copy(alpha = 0.4f))
                    }
                } else {
                    recentBookings.take(10).forEach { booking ->
                        VendorBookingRow(booking)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun QuickAccessCard(
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
        shape = RoundedCornerShape(16.dp),
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
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = gradient.first().copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = gradient.first(),
                            modifier = Modifier.size(18.dp)
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

@Composable
fun StatusSection(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = DeepBlue,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun VendorBookingRow(booking: VendorBooking) {
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
        shape = RoundedCornerShape(16.dp),
        color = DeepBlue,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(DarkNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.stationName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = booking.userEmail,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Text(
                    text = displayStatus,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(booking.bookingTime, color = ElectricGreen, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium)
                Text(booking.bookingDate, color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
