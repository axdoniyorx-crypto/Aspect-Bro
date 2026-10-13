package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import com.example.ui.components.ApkThreatCard
import com.example.ui.theme.CyberSafeEmerald
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberThreatRed
import com.example.ui.theme.CyberWarningAmber

@Composable
fun ApkScannerScreen(
    apkList: List<ApkThreatInfo>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onSelectApk: (ApkThreatInfo) -> Unit,
    onDeleteApk: (ApkThreatInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, THREATS, SAFE

    val filteredList = apkList.filter {
        val matchesQuery = it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "THREATS" -> it.severity == ThreatSeverity.CRITICAL || it.severity == ThreatSeverity.HIGH || it.severity == ThreatSeverity.MEDIUM
            "SAFE" -> it.severity == ThreatSeverity.SAFE || it.severity == ThreatSeverity.LOW
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar & Scan Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("APK qidirish...", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("apk_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberShieldCyan,
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = CyberSurfaceCard,
                    unfocusedContainerColor = CyberSurfaceCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onStartScan,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberShieldCyan)
                    .testTag("rescan_apks_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Qayta skanerlash",
                    tint = Color(0xFF00363D)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("Barchasi (${apkList.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberShieldCyan.copy(alpha = 0.2f),
                    selectedLabelColor = CyberShieldCyan
                )
            )

            val threatsCount = apkList.count { it.severity == ThreatSeverity.CRITICAL || it.severity == ThreatSeverity.HIGH }
            FilterChip(
                selected = selectedFilter == "THREATS",
                onClick = { selectedFilter = "THREATS" },
                label = { Text("Xavflilar ($threatsCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberThreatRed.copy(alpha = 0.2f),
                    selectedLabelColor = CyberThreatRed
                )
            )

            val safeCount = apkList.count { it.severity == ThreatSeverity.SAFE }
            FilterChip(
                selected = selectedFilter == "SAFE",
                onClick = { selectedFilter = "SAFE" },
                label = { Text("Tozalar ($safeCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberSafeEmerald.copy(alpha = 0.2f),
                    selectedLabelColor = CyberSafeEmerald
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results List
        if (filteredList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberSafeEmerald,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "APK fayllar topilmadi",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Xotirada yoki Downloads papkasida .apk fayli mavjud emas",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.filePath ?: it.packageName }) { apk ->
                    ApkThreatCard(
                        apkInfo = apk,
                        onClick = { onSelectApk(apk) },
                        onDelete = { onDeleteApk(apk) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
