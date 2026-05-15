package com.example.evgramacharge.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.evgramacharge.components.BottomNavBar
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.example.evgramacharge.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPersonalDetailsClick: () -> Unit,
    onChargingHistoryClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    
    var imageUriString by remember { mutableStateOf(preferenceManager.getProfileImagePath()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = preferenceManager.saveProfileImage(it)
            if (savedPath != null) {
                imageUriString = savedPath

                // Update Firebase Profile with the URI
                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(savedPath)
                )
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(fileUri)
                    .build()
                user?.updateProfile(profileUpdates)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onMapClick = onMapClick,
                onProfileClick = onProfileClick,
                selectedRoute = "profile"
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Profile Header with Glow and Image Picker
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = ElectricGreen.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ElectricGreen)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!imageUriString.isNullOrEmpty()) {
                            AsyncImage(
                                model = imageUriString,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Professional Default Avatar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(LightNavy, DeepBlue)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.displayName?.take(1)?.uppercase() ?: "E",
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricGreen
                                )
                            }
                        }
                    }
                }
                // Edit Badge / Image Picker Button
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = ElectricGreen,
                    border = androidx.compose.foundation.BorderStroke(3.dp, DarkNavy),
                    onClick = { launcher.launch("image/*") }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Picture", modifier = Modifier.padding(6.dp), tint = DarkNavy)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = user?.displayName ?: "Eco Rider",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "user@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            var totalBookings by remember { mutableIntStateOf(0) }

            // Real-time listener for user's total bookings
            LaunchedEffect(user?.email) {
                user?.email?.let { email ->
                    db.collection("bookings")
                        .whereEqualTo("userEmail", email)
                        .addSnapshotListener { snapshot, _ ->
                            totalBookings = snapshot?.size() ?: 0
                        }
                }
            }

            // Premium Stats Row - Removed Wallet, Clean Alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ProfileStatCard(
                    label = "Total Bookings", 
                    value = totalBookings.toString(),
                    icon = Icons.Default.EventNote,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    onClick = onChargingHistoryClick
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Settings Section
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = DeepBlue.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileMenuItem(icon = Icons.Default.Person, title = "Personal Details", subtitle = "Manage your info", onClick = onPersonalDetailsClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
                    ProfileMenuItem(icon = Icons.Default.Favorite, title = "My Favorites", subtitle = "Saved charging stations", onClick = onFavoritesClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
                    ProfileMenuItem(icon = Icons.Default.Refresh, title = "Charging History", subtitle = "Sessions & Receipts", onClick = onChargingHistoryClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
                    ProfileMenuItem(icon = Icons.Default.Settings, title = "App Settings", subtitle = "Notifications & Security", onClick = onAppSettingsClick)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logout Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.08f),
                    contentColor = Color.Red
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier,
        color = DeepBlue,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = ElectricGreen, fontWeight = FontWeight.ExtraBold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = LightNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(22.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.2f))
        }
    }
}
