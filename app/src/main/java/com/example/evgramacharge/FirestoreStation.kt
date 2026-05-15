package com.example.evgramacharge

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirestoreStation(
    val name: String = "Unknown Station",
    val location: String = "Unknown Location",
    val socketType: String = "Unknown",
    val availability: String = "Available",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val vendorEmail: String = "unknown@example.com",
    val facilities: List<String>? = null,
    val plugCount: Int? = null
)
