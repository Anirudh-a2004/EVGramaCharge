package com.example.evgramacharge.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialLatitude: Double,
    initialLongitude: Double,
    onLocationPicked: (Double, Double) -> Unit,
    onBackClick: () -> Unit
) {
    val initialPos = if (initialLatitude != 0.0) LatLng(initialLatitude, initialLongitude) else LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 15f)
    }

    var pickedLocation by remember { mutableStateOf(initialPos) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pick Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
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
        containerColor = DarkNavy,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onLocationPicked(pickedLocation.latitude, pickedLocation.longitude) },
                containerColor = ElectricGreen,
                contentColor = DarkNavy,
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text("Confirm Location", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    pickedLocation = latLng
                },
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = pickedLocation),
                    title = "Selected Location",
                    draggable = true
                )
            }
            
            // Helper overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = DarkNavy.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Tap on map to select station location",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
