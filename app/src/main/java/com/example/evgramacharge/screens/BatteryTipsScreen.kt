package com.example.evgramacharge.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryTipsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EV Battery Care", fontWeight = FontWeight.ExtraBold) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TipCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "Optimal Charging Range",
                description = "Keep your battery between 20% and 80% for daily use to extend its lifespan significantly."
            )
            TipCard(
                icon = Icons.Default.FlashOn,
                title = "Minimize Fast Charging",
                description = "While convenient, frequent DC fast charging can generate heat and degrade the battery faster than Level 2 charging."
            )
            TipCard(
                icon = Icons.Default.Thermostat,
                title = "Temperature Control",
                description = "Extreme heat and cold affect battery performance. Park in the shade on hot days and in a garage during winter if possible."
            )
            TipCard(
                icon = Icons.Default.HealthAndSafety,
                title = "Regular Software Updates",
                description = "Manufacturers often release updates that improve battery management system (BMS) efficiency."
            )
        }
    }
}

@Composable
fun TipCard(icon: ImageVector, title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LightNavy.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = ElectricGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
