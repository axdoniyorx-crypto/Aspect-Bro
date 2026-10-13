package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BlacklistHashEntity
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberThreatRed
import com.example.ui.theme.CyberWarningAmber

@Composable
fun CyberLabScreen(
    blacklistedHashes: List<BlacklistHashEntity>,
    isAccessibilityEnabled: Boolean,
    onAddBlacklistHash: (hash: String, name: String, category: String, desc: String) -> Unit,
    onRemoveBlacklistHash: (hash: String) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onTriggerSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var inputHash by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Banking Trojan") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Accessibility Security Notice Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isAccessibilityEnabled) Color(0xFF0F241E) else Color(0xFF2E1916)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAccessibilityEnabled) Color(0xFF00E676) else CyberThreatRed
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) Color(0xFF00E676) else CyberThreatRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAccessibilityEnabled) "Accessibility Interceptor Faol" else "Accessibility Servis O'chiq",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isAccessibilityEnabled) {
                            "Apk Soqchi har qanday yangi APK o'rnatilishini real vaqtda kuzatib turibdi va xavf tug'ilganda avtomatik 'Bekor qilish' tugmasini bosadi."
                        } else {
                            "Real vaqtda o'rnatish darchalarini tutib qolish va Cancel bosish uchun Accessibility ruxsati zarur."
                        },
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenAccessibilitySettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityEnabled) Color(0xFF1E293B) else CyberThreatRed
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lab_open_accessibility_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAccessibilityEnabled) "Accessibility Sozlamalari" else "Hozir Yoqish (Android Sozlamalari)",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Simulation Test Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧪 Kiber Hujum Simulyatori (Sandbox)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Haqiqiy telefon xavfsizligiga ziyon yetkazmasdan, xavfli APK o'rnatilishini to'xtatish tizimi qanday ishlashini sinab ko'ring.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onTriggerSimulation,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberThreatRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_malware_intercept_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Zararli O'rnatishni Sinovdan O'tkazish", fontSize = 12.sp)
                    }
                }
            }
        }

        // Threat Database Header & Add Button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Malware Imzolari Bazasi",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lokal qora ro'yxat (${blacklistedHashes.size} ta xesh)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { showAddDialog = !showAddDialog },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberShieldCyan)
                        .testTag("add_hash_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Xesh qo'shish",
                        tint = Color(0xFF00363D)
                    )
                }
            }
        }

        // Add Hash Input Panel
        if (showAddDialog) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Yangi Virus Imzosini Kiritish",
                            color = CyberShieldCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("Virus nomi (masalan: Trojan.Banker.Cerberus)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputHash,
                            onValueChange = { inputHash = it },
                            placeholder = { Text("SHA-256 xesh kodi (64 ta belgi)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(onClick = { showAddDialog = false }) {
                                Text("Bekor qilish", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (inputHash.isNotBlank() && inputName.isNotBlank()) {
                                        onAddBlacklistHash(
                                            inputHash,
                                            inputName,
                                            inputCategory,
                                            "Qo'lda kiritilgan zararli xesh"
                                        )
                                        inputHash = ""
                                        inputName = ""
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberShieldCyan),
                                modifier = Modifier.testTag("save_custom_hash_btn")
                            ) {
                                Text("Saqlash", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // List of Known Hashes
        items(blacklistedHashes, key = { it.sha256 }) { hashItem ->
            BlacklistItemCard(
                item = hashItem,
                onDelete = { onRemoveBlacklistHash(hashItem.sha256) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BlacklistItemCard(
    item: BlacklistHashEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF233554)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = CyberThreatRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.threatName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.threatCategory,
                    color = CyberWarningAmber,
                    fontSize = 11.sp
                )
                Text(
                    text = item.sha256,
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "O'chirish",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
