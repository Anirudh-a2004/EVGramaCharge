package com.example.evgramacharge.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStationScreen(
    pickedLat: Double = 0.0,
    pickedLng: Double = 0.0,
    onPickLocationClick: () -> Unit,
    onBackClick: () -> Unit,
    // Hoisted states to fix reset issue
    hoistedStationName: String,
    onStationNameChange: (String) -> Unit,
    hoistedLocationAddress: String,
    onLocationAddressChange: (String) -> Unit,
    hoistedSocketType: String,
    onSocketTypeChange: (String) -> Unit,
    hoistedAvailability: String,
    onAvailabilityChange: (String) -> Unit,
    hoistedPlugCount: String,
    onPlugCountChange: (String) -> Unit,
    hoistedSelectedFacilities: SnapshotStateList<String>
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var latitude by remember { mutableStateOf(if (pickedLat != 0.0) pickedLat.toString() else "") }
    var longitude by remember { mutableStateOf(if (pickedLng != 0.0) pickedLng.toString() else "") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val allFacilities = listOf(
        "Toilets", "Restrooms", "Drinking Water", "Snacks & Drinks",
        "Resting Area", "WiFi", "Parking", "Air Pump", "Cafe",
        "Waiting Lounge", "CCTV Security", "24/7 Availability"
    )

    LaunchedEffect(pickedLat, pickedLng) {
        if (pickedLat != 0.0 && pickedLng != 0.0) {
            latitude = pickedLat.toString()
            longitude = pickedLng.toString()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        latitude = loc.latitude.toString()
                        longitude = loc.longitude.toString()
                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) { }
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("List Your Station", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = ElectricGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔌", fontSize = 40.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Join our network", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
            Text(text = "Help other EV owners and start earning", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernTextField(value = hoistedStationName, onValueChange = onStationNameChange, label = "Station Name", icon = Icons.Default.Info)
                ModernTextField(value = hoistedLocationAddress, onValueChange = onLocationAddressChange, label = "Full Address", icon = Icons.Default.LocationOn)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernTextField(value = hoistedSocketType, onValueChange = onSocketTypeChange, label = "Type", icon = Icons.Default.Settings, modifier = Modifier.weight(1f))
                    ModernTextField(
                        value = hoistedPlugCount, 
                        onValueChange = { newVal: String -> if (newVal.all { char: Char -> char.isDigit() }) onPlugCountChange(newVal) },
                        label = "Plugs", 
                        icon = Icons.Default.Power, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "Station Coordinates", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                    if (loc != null) {
                                        latitude = loc.latitude.toString()
                                        longitude = loc.longitude.toString()
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LightNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onPickLocationClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LightNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick on Map", color = Color.White, fontSize = 12.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModernTextField(value = latitude, onValueChange = { latitude = it }, label = "Latitude", icon = Icons.Default.LocationSearching, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    ModernTextField(value = longitude, onValueChange = { longitude = it }, label = "Longitude", icon = Icons.Default.LocationSearching, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Station Facilities", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allFacilities.forEach { facility ->
                        FilterChip(
                            selected = hoistedSelectedFacilities.contains(facility),
                            onClick = {
                                if (hoistedSelectedFacilities.contains(facility)) {
                                    hoistedSelectedFacilities.remove(facility)
                                } else {
                                    hoistedSelectedFacilities.add(facility)
                                }
                            },
                            label = { Text(facility) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = LightNavy.copy(alpha = 0.3f),
                                labelColor = Color.White.copy(alpha = 0.6f),
                                selectedContainerColor = ElectricGreen.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricGreen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = hoistedSelectedFacilities.contains(facility),
                                borderColor = Color.White.copy(alpha = 0.1f),
                                selectedBorderColor = ElectricGreen.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val latVal = latitude.toDoubleOrNull()
                    val lngVal = longitude.toDoubleOrNull()
                    val plugs = hoistedPlugCount.toIntOrNull() ?: 1

                    if (hoistedStationName.isBlank()) {
                        message = "Station name is required."
                        return@Button
                    }
                    if (latVal == null || lngVal == null) {
                        message = "Please provide valid coordinates."
                        return@Button
                    }

                    isLoading = true
                    isSuccess = false
                    val station = hashMapOf(
                        "name" to hoistedStationName.trim(),
                        "location" to hoistedLocationAddress.trim(),
                        "socketType" to hoistedSocketType.trim(),
                        "availability" to hoistedAvailability,
                        "latitude" to latVal,
                        "longitude" to lngVal,
                        "plugCount" to plugs,
                        "vendorEmail" to (userEmail ?: ""),
                        "facilities" to hoistedSelectedFacilities.toList(),
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("stations")
                        .add(station)
                        .addOnSuccessListener {
                            isLoading = false
                            isSuccess = true
                            message = "Station registered successfully!"
                            
                            val notification = hashMapOf(
                                "userEmail" to (userEmail ?: ""),
                                "title" to "Station Listed",
                                "message" to "Station '${hoistedStationName}' has been successfully registered.",
                                "timestamp" to System.currentTimeMillis(),
                                "type" to "update",
                                "isVendor" to true
                            )
                            db.collection("notifications").add(notification)

                            onStationNameChange(""); onLocationAddressChange(""); onSocketTypeChange(""); onAvailabilityChange("Available")
                            latitude = ""; longitude = ""; onPlugCountChange("")
                            hoistedSelectedFacilities.clear()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            message = "Error: ${e.localizedMessage ?: "Registration failed."}"
                        }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = DarkNavy, strokeWidth = 3.dp)
                } else {
                    Text("Register Station", color = DarkNavy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }

            if (message.isNotEmpty()) {
                Surface(
                    modifier = Modifier.padding(top = 24.dp),
                    color = if (isSuccess) ElectricGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message,
                        color = if (isSuccess) ElectricGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(20.dp)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricGreen,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = ElectricGreen,
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.5f),
            focusedContainerColor = LightNavy.copy(alpha = 0.3f),
            unfocusedContainerColor = LightNavy.copy(alpha = 0.3f),
            disabledContainerColor = LightNavy.copy(alpha = 0.1f),
            cursorColor = ElectricGreen
        )
    )
}
