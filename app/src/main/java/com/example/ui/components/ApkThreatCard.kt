package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import com.example.ui.theme.CyberSafeEmerald
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberThreatRed
import com.example.ui.theme.CyberWarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApkThreatCard(
    apkInfo: ApkThreatInfo,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val severityColor = when (apkInfo.severity) {
        ThreatSeverity.SAFE -> CyberSafeEmerald
        ThreatSeverity.LOW -> CyberShieldCyan
        ThreatSeverity.MEDIUM -> CyberWarningAmber
        ThreatSeverity.HIGH, ThreatSeverity.CRITICAL -> CyberThreatRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, severityColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("apk_threat_card_${apkInfo.packageName}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon + Names + Severity Score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(severityColor.copy(alpha = 0.15f))
                            .border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (apkInfo.threatScore > 50) Icons.Default.Warning else Icons.Default.Android,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = apkInfo.appName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = apkInfo.packageName,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Threat Score Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(severityColor.copy(alpha = 0.18f))
                        .border(1.dp, severityColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${apkInfo.threatScore}/100",
                        color = severityColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub info: Version, Size, Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hajm: ${apkInfo.formattedSize} • v${apkInfo.versionName}",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )

                Text(
                    text = apkInfo.severity.labelUz,
                    color = severityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dangerous permissions preview chips
            if (apkInfo.dangerousPermissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    apkInfo.dangerousPermissions.take(3).forEach { perm ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberThreatRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = perm,
                                color = Color(0xFFFF8A9E),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                    if (apkInfo.dangerousPermissions.size > 3) {
                        Text(
                            text = "+${apkInfo.dangerousPermissions.size - 3} ta boshqa",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Primary reason if threat
            if (apkInfo.threatReasons.isNotEmpty() && apkInfo.threatScore > 30) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠️ " + apkInfo.threatReasons.first(),
                    color = CyberThreatRed,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action row if delete is available
            if (onDelete != null && !apkInfo.isInstalled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_apk_${apkInfo.packageName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "APK ni o'chirish",
                            tint = CyberThreatRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
