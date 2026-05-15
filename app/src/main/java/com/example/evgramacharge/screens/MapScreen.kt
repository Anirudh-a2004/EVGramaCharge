package com.example.evgramacharge.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.evgramacharge.FirestoreStation
import com.example.evgramacharge.components.BottomNavBar
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.example.evgramacharge.ui.theme.DeepBlue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBookClick: (String, String, String, String, Double, Double, String, List<String>, Int) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    var firestoreStations by remember { mutableStateOf(listOf<FirestoreStation>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    val filteredStations = remember(searchQuery, firestoreStations) {
        if (searchQuery.isBlank()) emptyList()
        else firestoreStations.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    DisposableEffect(Unit) {
        val listener: ListenerRegistration = db.collection("stations")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("MapScreen", "Error fetching stations", error)
                    return@addSnapshotListener
                }
                
                if (value != null) {
                    firestoreStations = value.documents.mapNotNull { document ->
                        try {
                            FirestoreStation(
                                name = document.getString("name") ?: "Unknown Station",
                                location = document.getString("location") ?: "Unknown Location",
                                socketType = document.getString("socketType") ?: "Unknown",
                                availability = document.getString("availability") ?: "Available",
                                latitude = document.getDouble("latitude") ?: 0.0,
                                longitude = document.getDouble("longitude") ?: 0.0,
                                vendorEmail = document.getString("vendorEmail") ?: "unknown@example.com",
                                facilities = (document.get("facilities") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                plugCount = document.getLong("plugCount")?.toInt() ?: 1
                            )
                        } catch (e: Exception) {
                            Log.e("MapScreen", "Error parsing station: ${document.id}", e)
                            null
                        }
                    }
                }
            }
        
        onDispose {
            listener.remove()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    val defaultLocation = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onMapClick = onMapClick,
                onProfileClick = onProfileClick,
                selectedRoute = "map"
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (!locationPermissionGranted) {
                LocationPermissionContent { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false, 
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    )
                ) {
                    firestoreStations.forEach { station ->
                        Marker(
                            state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                            title = station.name,
                            snippet = "${station.socketType} • ${station.availability}",
                            onClick = {
                                onBookClick(
                                    station.name, 
                                    station.location,
                                    station.socketType,
                                    station.availability,
                                    station.latitude, 
                                    station.longitude,
                                    station.vendorEmail,
                                    station.facilities ?: emptyList(),
                                    station.plugCount ?: 1
                                )
                                true
                            }
                        )
                    }
                }

                // Professional Floating Search Bar
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkNavy.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        shadowElevation = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ElectricGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            TextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    isSearchActive = it.isNotBlank()
                                },
                                placeholder = { Text("Search charging stations...", color = Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { 
                                    focusManager.clearFocus()
                                    isSearchActive = false
                                }),
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { 
                                            searchQuery = "" 
                                            isSearchActive = false
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Search Suggestions Dropdown
                    AnimatedVisibility(
                        visible = isSearchActive && filteredStations.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = DeepBlue.copy(alpha = 0.98f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shadowElevation = 12.dp
                        ) {
                            LazyColumn {
                                items(filteredStations) { station ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = station.name
                                                isSearchActive = false
                                                focusManager.clearFocus()
                                                scope.launch {
                                                    cameraPositionState.animate(
                                                        CameraUpdateFactory.newLatLngZoom(
                                                            LatLng(station.latitude, station.longitude), 
                                                            16f
                                                        )
                                                    )
                                                }
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.EvStation, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(station.name, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("${station.socketType} • ${station.location}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1)
                                        }
                                    }
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }

                // Map Control Buttons
                Column(
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                        .align(Alignment.BottomEnd),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { 
                            // Try to animate to user location or default
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f))
                            }
                        },
                        containerColor = DeepBlue,
                        contentColor = ElectricGreen,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationPermissionContent(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = ElectricGreen.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(60.dp), tint = ElectricGreen)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Enable Location Access", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("EVGramaCharge needs your location to find the nearest charging stations and provide navigation.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGrantClick, 
            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen), 
            shape = RoundedCornerShape(18.dp), 
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Grant Permission", color = DarkNavy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}
