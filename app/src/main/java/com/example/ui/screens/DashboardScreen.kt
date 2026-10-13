package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ThreatRecordEntity
import com.example.model.ThreatSeverity
import com.example.scanner.ScanProgress
import com.example.ui.components.CyberShieldHero
import com.example.ui.theme.CyberSafeEmerald
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberThreatRed
import com.example.ui.theme.CyberWarningAmber
import com.example.viewmodel.SecurityStatus

@Composable
fun DashboardScreen(
    status: SecurityStatus,
    isRealtimeEnabled: Boolean,
    isAccessibilityEnabled: Boolean,
    isScanning: Boolean,
    scanProgress: ScanProgress,
    threatRecords: List<ThreatRecordEntity>,
    totalApksCount: Int,
    threatsFoundCount: Int,
    onToggleRealtime: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onStartStorageScan: () -> Unit,
    onStartInstalledAppsScan: () -> Unit,
    onTriggerSimulation: () -> Unit,
    onNavigateToScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CyberShieldHero(
                status = status,
                isRealtimeEnabled = isRealtimeEnabled,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onToggleRealtime = onToggleRealtime,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )
        }

        // Live Scanning Progress Banner
        item {
            AnimatedVisibility(visible = isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Skanerlash jarayoni...",
                                color = CyberShieldCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${scanProgress.scannedCount} / ${scanProgress.totalFound}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val progressFraction = if (scanProgress.totalFound > 0) {
                            scanProgress.scannedCount.toFloat() / scanProgress.totalFound.toFloat()
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyberShieldCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scanProgress.currentItemName,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Stats Matrix Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Skanerlangan",
                    value = totalApksCount.toString(),
                    color = CyberShieldCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Tahdidlar",
                    value = threatsFoundCount.toString(),
                    color = if (threatsFoundCount > 0) CyberThreatRed else CyberSafeEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Bloklangan",
                    value = threatRecords.filter { it.wasBlocked }.size.toString(),
                    color = CyberWarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Buttons Row
        item {
            Text(
                text = "Tezkor Amallar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartStorageScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_storage_scan_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberShieldCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF00363D),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APK Skaner",
                        color = Color(0xFF00363D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onStartInstalledAppsScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_installed_scan_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ilovalar Auditi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Test Simulation Button
            OutlinedButton(
                onClick = onTriggerSimulation,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trigger_simulation_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberThreatRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberThreatRed.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zararli O'rnatishni Sinovdan O'tkazish (Demo Test)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Recent Blocked Intercepts History
        item {
            Text(
                text = "Kiber Xavfsizlik Jurnali (Bloklanganlar)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (threatRecords.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberSurfaceCard)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyberSafeEmerald,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hozircha hech qanday tahdid bloklanmagan",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(threatRecords.take(5)) { record ->
                BlockedRecordItem(record = record)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun BlockedRecordItem(record: ThreatRecordEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF160E18))
            .border(1.dp, CyberThreatRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberThreatRed.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = CyberThreatRed,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.appName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = record.reasonsSummary,
                color = Color(0xFFFFB4AB),
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        Text(
            text = "BLOKLANDI",
            color = CyberThreatRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
