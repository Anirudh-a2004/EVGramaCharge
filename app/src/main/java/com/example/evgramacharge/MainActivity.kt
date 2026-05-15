package com.example.evgramacharge

import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.evgramacharge.screens.*
import com.example.evgramacharge.ui.theme.EVGramaChargeTheme
import com.example.evgramacharge.utils.PreferenceManager
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val preferenceManager = remember { PreferenceManager(context) }
            
            // Persistent state for Theme and Language
            var isDarkMode by remember { mutableStateOf(preferenceManager.isDarkMode()) }

            EVGramaChargeTheme(darkTheme = isDarkMode) {
                val auth = FirebaseAuth.getInstance()

                var currentScreen by remember {
                    mutableStateOf(
                        if (auth.currentUser != null)
                            "role"
                        else
                            "login"
                    )
                }

                // Station Selection States
                var selectedStationId by remember { mutableStateOf("") }
                var selectedStationName by remember { mutableStateOf("") }
                var selectedLocation by remember { mutableStateOf("") }
                var selectedSocketType by remember { mutableStateOf("") }
                var selectedAvailability by remember { mutableStateOf("") }
                var selectedLatitude by remember { mutableDoubleStateOf(0.0) }
                var selectedLongitude by remember { mutableDoubleStateOf(0.0) }
                var selectedVendorEmail by remember { mutableStateOf("") }
                var selectedFacilities by remember { mutableStateOf(listOf<String>()) }
                var selectedPlugCount by remember { mutableIntStateOf(1) }
                
                // Add Station Form State (Hoisted to prevent reset during location picking)
                var formStationName by rememberSaveable { mutableStateOf("") }
                var formLocationAddress by rememberSaveable { mutableStateOf("") }
                var formSocketType by rememberSaveable { mutableStateOf("") }
                var formAvailability by rememberSaveable { mutableStateOf("Available") }
                var formPlugCount by rememberSaveable { mutableStateOf("") }
                val formSelectedFacilities = remember { mutableStateListOf<String>() }

                // State for Smart Location Selection
                var pickedLatitude by remember { mutableDoubleStateOf(0.0) }
                var pickedLongitude by remember { mutableDoubleStateOf(0.0) }

                // Back navigation map for gesture support
                val backNavigationMap = mapOf(
                    "home" to "role",
                    "map" to "home",
                    "stationDetails" to "map",
                    "booking" to "stationDetails",
                    "profile" to "home",
                    "vendor" to "role",
                    // Vendor-specific back routes
                    "myStations" to "vendor",
                    "vendorBookings" to "vendor",
                    "vendorProfile" to "vendor",
                    "stationPerformance" to "myStations",
                    "addStation" to "vendor",
                    "locationPicker" to "addStation",
                    "notifications" to "home",
                    "vendorNotifications" to "vendor",
                    "allStations" to "home",
                    "favorites" to "profile",
                    "personalDetails" to "profile",
                    "chargingHistory" to "profile",
                    "appSettings" to "profile",
                    "batteryTips" to "home",
                    "privacySecurity" to "appSettings"
                )

                // Premium Screen Transitions with Gesture Support
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                // Detect right swipe (back gesture)
                                if (dragAmount > 100) { // Minimum swipe distance
                                    backNavigationMap[currentScreen]?.let { backScreen ->
                                        currentScreen = backScreen
                                    }
                                }
                            }
                        }
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f) togetherWith
                            fadeOut(animationSpec = tween(400))
                        },
                        label = "ScreenTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            "login" -> {
                                LoginScreen(
                                    onLoginClick = { currentScreen = "role" },
                                    onSignupNavigate = { currentScreen = "signup" }
                                )
                            }

                            "signup" -> {
                                SignupScreen(
                                    onSignupClick = { currentScreen = "role" },
                                    onLoginNavigate = { currentScreen = "login" }
                                )
                            }

                            "role" -> {
                                RoleSelectionScreen(
                                    onUserClick = { currentScreen = "home" },
                                    onVendorClick = { currentScreen = "vendor" }
                                )
                            }

                            "home" -> {
                                EVHomeScreen(
                                    onHomeClick = { currentScreen = "home" },
                                    onMapClick = { currentScreen = "map" },
                                    onProfileClick = { currentScreen = "profile" },
                                    onNotificationsClick = { currentScreen = "notifications" },
                                    onSeeAllStationsClick = { currentScreen = "allStations" },
                                    onFavoritesClick = { currentScreen = "favorites" },
                                    onBatteryTipsClick = { currentScreen = "batteryTips" },
                                    onChargingHistoryClick = { currentScreen = "chargingHistory" },
                                    onStationClick = { name, location, socket, avail, lat, lng, vEmail, facilities, plugs ->
                                        selectedStationName = name
                                        selectedLocation = location
                                        selectedSocketType = socket
                                        selectedAvailability = avail
                                        selectedLatitude = lat
                                        selectedLongitude = lng
                                        selectedVendorEmail = vEmail
                                        selectedFacilities = facilities
                                        selectedPlugCount = plugs
                                        currentScreen = "stationDetails"
                                    },
                                    onBackClick = { currentScreen = "role" }
                                )
                            }

                            "map" -> {
                                MapScreen(
                                    onHomeClick = { currentScreen = "home" },
                                    onMapClick = { currentScreen = "map" },
                                    onProfileClick = { currentScreen = "profile" },
                                    onBookClick = { name, location, socket, avail, lat, lng, vEmail, facilities, plugs ->
                                        selectedStationName = name
                                        selectedLocation = location
                                        selectedSocketType = socket
                                        selectedAvailability = avail
                                        selectedLatitude = lat
                                        selectedLongitude = lng
                                        selectedVendorEmail = vEmail
                                        selectedFacilities = facilities
                                        selectedPlugCount = plugs
                                        currentScreen = "stationDetails"
                                    }
                                )
                            }

                            "stationDetails" -> {
                                StationDetailsScreen(
                                    stationName = selectedStationName,
                                    location = selectedLocation,
                                    socketType = selectedSocketType,
                                    availability = selectedAvailability,
                                    latitude = selectedLatitude,
                                    longitude = selectedLongitude,
                                    facilities = selectedFacilities,
                                    plugCount = selectedPlugCount,
                                    onBookNowClick = { currentScreen = "booking" },
                                    onBackClick = { currentScreen = "map" }
                                )
                            }

                            "booking" -> {
                                BookingScreen(
                                    stationName = selectedStationName,
                                    vendorEmail = selectedVendorEmail,
                                    totalPlugs = selectedPlugCount,
                                    latitude = selectedLatitude,
                                    longitude = selectedLongitude,
                                    onBackClick = { currentScreen = "stationDetails" }
                                )
                            }

                            "profile" -> {
                                ProfileScreen(
                                    onHomeClick = { currentScreen = "home" },
                                    onMapClick = { currentScreen = "map" },
                                    onProfileClick = { currentScreen = "profile" },
                                    onLogoutClick = {
                                        auth.signOut()
                                        currentScreen = "login"
                                    },
                                    onPersonalDetailsClick = { currentScreen = "personalDetails" },
                                    onChargingHistoryClick = { currentScreen = "chargingHistory" },
                                    onAppSettingsClick = { currentScreen = "appSettings" },
                                    onFavoritesClick = { currentScreen = "favorites" }
                                )
                            }

                            "vendor" -> {
                                VendorDashboardScreen(
                                    onAddStationClick = { currentScreen = "addStation" },
                                    onNotificationsClick = { currentScreen = "vendorNotifications" },
                                    onBackClick = { currentScreen = "role" },
                                    // Vendor-only quick access should open vendor screens
                                    onMapClick = { currentScreen = "map" },
                                    onBookingsClick = { currentScreen = "vendorBookings" },
                                    onNearbyStationsClick = { currentScreen = "myStations" },
                                    onProfileClick = { currentScreen = "vendorProfile" }
                                )
                            }

                            // Vendor: Manage Stations
                            "myStations" -> {
                                MyStationsScreen(
                                    onBackClick = { currentScreen = "vendor" },
                                    onStationClick = { stationId ->
                                        // open station performance for the selected station
                                        selectedStationId = stationId
                                        currentScreen = "stationPerformance"
                                    }
                                )
                            }

                            // Vendor: Station Bookings
                            "vendorBookings" -> {
                                VendorBookingsScreen(
                                    onBackClick = { currentScreen = "vendor" }
                                )
                            }

                            // Vendor: Profile (vendor-focused)
                            "vendorProfile" -> {
                                VendorProfileScreen(
                                    onBackClick = { currentScreen = "vendor" },
                                    onLogoutClick = {
                                        auth.signOut()
                                        currentScreen = "login"
                                    },
                                    onStationsClick = { currentScreen = "myStations" },
                                    onBookingsClick = { currentScreen = "vendorBookings" }
                                )
                            }

                            // Vendor: Station Performance view
                            "stationPerformance" -> {
                                StationPerformanceScreen(
                                    stationId = selectedStationId,
                                    onBackClick = { currentScreen = "myStations" }
                                )
                            }

                            "addStation" -> {
                                AddStationScreen(
                                    pickedLat = pickedLatitude,
                                    pickedLng = pickedLongitude,
                                    onPickLocationClick = { currentScreen = "locationPicker" },
                                    onBackClick = {
                                        pickedLatitude = 0.0
                                        pickedLongitude = 0.0
                                        currentScreen = "vendor"
                                    },
                                    // Hoisted states to fix reset issue
                                    hoistedStationName = formStationName,
                                    onStationNameChange = { formStationName = it },
                                    hoistedLocationAddress = formLocationAddress,
                                    onLocationAddressChange = { formLocationAddress = it },
                                    hoistedSocketType = formSocketType,
                                    onSocketTypeChange = { formSocketType = it },
                                    hoistedAvailability = formAvailability,
                                    onAvailabilityChange = { formAvailability = it },
                                    hoistedPlugCount = formPlugCount,
                                    onPlugCountChange = { formPlugCount = it },
                                    hoistedSelectedFacilities = formSelectedFacilities
                                )
                            }

                            "locationPicker" -> {
                                LocationPickerScreen(
                                    initialLatitude = pickedLatitude,
                                    initialLongitude = pickedLongitude,
                                    onLocationPicked = { lat, lng ->
                                        pickedLatitude = lat
                                        pickedLongitude = lng
                                        currentScreen = "addStation"
                                    },
                                    onBackClick = { currentScreen = "addStation" }
                                )
                            }

                            "notifications" -> {
                                NotificationScreen(
                                    isVendor = false,
                                    onBackClick = { currentScreen = "home" }
                                )
                            }

                            "vendorNotifications" -> {
                                NotificationScreen(
                                    isVendor = true,
                                    onBackClick = { currentScreen = "vendor" }
                                )
                            }

                            "allStations" -> {
                                AllStationsScreen(
                                    onBackClick = { currentScreen = "home" },
                                    onStationClick = { name, location, socket, avail, lat, lng, vEmail, facilities, plugs ->
                                        selectedStationName = name
                                        selectedLocation = location
                                        selectedSocketType = socket
                                        selectedAvailability = avail
                                        selectedLatitude = lat
                                        selectedLongitude = lng
                                        selectedVendorEmail = vEmail
                                        selectedFacilities = facilities
                                        selectedPlugCount = plugs
                                        currentScreen = "stationDetails"
                                    }
                                )
                            }

                            "favorites" -> {
                                FavoritesScreen(
                                    onBackClick = { currentScreen = "profile" },
                                    onStationClick = { name, location, socket, avail, lat, lng, vEmail, facilities, plugs ->
                                        selectedStationName = name
                                        selectedLocation = location
                                        selectedSocketType = socket
                                        selectedAvailability = avail
                                        selectedLatitude = lat
                                        selectedLongitude = lng
                                        selectedVendorEmail = vEmail
                                        selectedFacilities = facilities
                                        selectedPlugCount = plugs
                                        currentScreen = "stationDetails"
                                    }
                                )
                            }

                            "personalDetails" -> {
                                PersonalDetailsScreen(
                                    onBackClick = { currentScreen = "profile" }
                                )
                            }

                            "chargingHistory" -> {
                                ChargingHistoryScreen(
                                    onBackClick = { currentScreen = "profile" }
                                )
                            }

                            "appSettings" -> {
                                AppSettingsScreen(
                                    onBackClick = { currentScreen = "profile" },
                                    isDarkMode = isDarkMode,
                                    onDarkModeToggle = { theme ->
                                        isDarkMode = theme
                                        preferenceManager.saveTheme(theme)
                                    },
                                    onPrivacyClick = { currentScreen = "privacySecurity" }
                                )
                            }

                            "batteryTips" -> {
                                BatteryTipsScreen(
                                    onBackClick = { currentScreen = "home" }
                                )
                            }



                            "privacySecurity" -> {
                                PrivacySecurityScreen(
                                    onBackClick = { currentScreen = "appSettings" }
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}
