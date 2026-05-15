package com.example.evgramacharge.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val type: String = "update", // "booking", "reminder", "nearby", "update"
    val isRead: Boolean = false,
    val isVendor: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    isVendor: Boolean,
    onBackClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    
    var notifications by remember { mutableStateOf(listOf<NotificationItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userEmail, isVendor) {
        if (userEmail.isNotEmpty()) {
            db.collection("notifications")
                .whereEqualTo("userEmail", userEmail)
                .whereEqualTo("isVendor", isVendor)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
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
                title = { 
                    Text(
                        if (isVendor) "Vendor Alerts" else "Activity Center", 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp).background(LightNavy.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { 
                            notifications.forEach { 
                                db.collection("notifications").document(it.id).delete()
                            }
                        }) {
                            Text("Clear All", color = ElectricGreen.copy(alpha = 0.7f))
                        }
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricGreen)
            }
        } else if (notifications.isEmpty()) {
            EmptyNotificationsState(modifier = Modifier.padding(paddingValues), isVendor = isVendor)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    val icon = when (notification.type) {
        "booking" -> Icons.Default.CalendarMonth
        "reminder" -> Icons.Default.Bolt
        "nearby" -> Icons.Default.Notifications
        else -> Icons.Default.Update
    }

    val timeStr = remember(notification.timestamp) {
        if (notification.timestamp == 0L) ""
        else {
            val now = System.currentTimeMillis()
            val diff = now - notification.timestamp
            when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(notification.timestamp))
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (notification.isRead) LightNavy.copy(alpha = 0.2f) else LightNavy.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (notification.isRead) Color.White.copy(alpha = 0.05f) else ElectricGreen.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (notification.isRead) DeepBlue.copy(alpha = 0.5f) else ElectricGreen.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon, 
                            contentDescription = null, 
                            tint = if (notification.isRead) Color.White.copy(alpha = 0.4f) else ElectricGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(ElectricGreen, CircleShape)
                            .border(2.dp, DarkNavy, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (notification.isRead) Color.White.copy(alpha = 0.6f) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(modifier: Modifier = Modifier, isVendor: Boolean) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = LightNavy.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationsNone, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "All Caught Up!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (isVendor) "You'll be notified here about new bookings and station updates."
                else "You don't have any new notifications at the moment. We'll alert you for your next booking!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
