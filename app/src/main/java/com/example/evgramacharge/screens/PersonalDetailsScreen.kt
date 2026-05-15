package com.example.evgramacharge.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.evgramacharge.ui.theme.DarkNavy
import com.example.evgramacharge.ui.theme.ElectricGreen
import com.example.evgramacharge.ui.theme.LightNavy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(onBackClick: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current

    var fullName by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch existing phone number from Firestore if available
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    phoneNumber = doc.getString("phoneNumber") ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Personal Details", fontWeight = FontWeight.ExtraBold) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Profile Information",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricGreen
            )

            ModernTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                icon = Icons.Default.Person
            )

            ModernTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                enabled = false
            )

            ModernTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                icon = Icons.Default.Phone
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        isLoading = true
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.trim())
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userData = hashMapOf(
                                        "fullName" to fullName.trim(),
                                        "phoneNumber" to phoneNumber.trim()
                                    )
                                    // Use set with merge to ensure document exists and fields are synced
                                    db.collection("users").document(user.uid)
                                        .set(userData, SetOptions.merge())
                                        .addOnCompleteListener { firestoreTask ->
                                            isLoading = false
                                            if (firestoreTask.isSuccessful) {
                                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                                onBackClick()
                                            } else {
                                                Toast.makeText(context, "Auth updated, but sync failed.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", color = DarkNavy, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
