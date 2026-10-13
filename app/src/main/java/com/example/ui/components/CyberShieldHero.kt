package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberSafeEmerald
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberThreatRed
import com.example.ui.theme.CyberWarningAmber
import com.example.viewmodel.SecurityStatus

@Composable
fun CyberShieldHero(
    status: SecurityStatus,
    isRealtimeEnabled: Boolean,
    isAccessibilityEnabled: Boolean,
    onToggleRealtime: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_pulse"
    )

    val shieldColor by animateColorAsState(
        targetValue = when (status) {
            SecurityStatus.SECURE -> CyberSafeEmerald
            SecurityStatus.WARNING -> CyberWarningAmber
            SecurityStatus.DANGER -> CyberThreatRed
        },
        label = "shield_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberSurfaceCard,
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.dp, shieldColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Animated Shield Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (isRealtimeEnabled) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(shieldColor.copy(alpha = 0.12f))
                    .border(2.dp, shieldColor.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = when (status) {
                        SecurityStatus.SECURE -> Icons.Default.GppGood
                        SecurityStatus.WARNING -> Icons.Default.GppMaybe
                        SecurityStatus.DANGER -> Icons.Default.GppBad
                    },
                    contentDescription = "Xavfsizlik qalqoni",
                    tint = shieldColor,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = status.titleUz,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = status.descriptionUz,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Real-time Guard Switch Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF131D33))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Real-vaqtda APK O'rnatish Qalqoni",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isRealtimeEnabled) "APK ochilganda avtomatik skanerlash faol" else "Himoya o'chirilgan!",
                        color = if (isRealtimeEnabled) CyberShieldCyan else CyberThreatRed,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = isRealtimeEnabled,
                    onCheckedChange = onToggleRealtime,
                    modifier = Modifier.testTag("realtime_guard_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberSafeEmerald,
                        checkedTrackColor = Color(0xFF004D27)
                    )
                )
            }

            if (!isAccessibilityEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enable_accessibility_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyberWarningAmber
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarningAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accessibility Sozlamasini Yoqish (Zarur)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
