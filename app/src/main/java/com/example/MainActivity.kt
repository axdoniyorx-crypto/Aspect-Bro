package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import com.example.ui.components.SecurityAlertModal
import com.example.ui.components.ThreatDetailDialog
import com.example.ui.screens.ApkScannerScreen
import com.example.ui.screens.CyberLabScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InstalledAppsScreen
import com.example.ui.theme.ApkSoqchiTheme
import com.example.ui.theme.CyberSafeEmerald
import com.example.ui.theme.CyberShieldCyan
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberThreatRed
import com.example.viewmodel.ApkGuardViewModel
import com.example.viewmodel.SecurityStatus

class MainActivity : ComponentActivity() {

    private val viewModel: ApkGuardViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check & request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ApkSoqchiTheme {
                MainContent(
                    viewModel = viewModel,
                    onOpenAccessibilitySettings = { openAccessibilitySettings() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceStatus()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: ApkGuardViewModel,
    onOpenAccessibilitySettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isRealtimeEnabled by viewModel.isRealtimeGuardEnabled.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()

    val storageApks by viewModel.storageApks.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val threatRecords by viewModel.threatRecords.collectAsStateWithLifecycle()
    val blacklistedHashes by viewModel.blacklistedHashes.collectAsStateWithLifecycle()

    val selectedDetail by viewModel.selectedDetail.collectAsStateWithLifecycle()
    val activeAlertModal by viewModel.activeAlertModal.collectAsStateWithLifecycle()
    val messageSnackbar by viewModel.messageSnackbar.collectAsStateWithLifecycle()

    val overallStatus = viewModel.getOverallStatus()

    LaunchedEffect(messageSnackbar) {
        messageSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Apk Soqchi",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Apk Soqchi Logo",
                        tint = when (overallStatus) {
                            SecurityStatus.SECURE -> CyberSafeEmerald
                            SecurityStatus.WARNING -> CyberShieldCyan
                            SecurityStatus.DANGER -> CyberThreatRed
                        },
                        modifier = Modifier
                            .padding(start = 12.dp, end = 6.dp)
                            .size(26.dp)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(
                                color = if (isRealtimeEnabled && isAccessibilityEnabled)
                                    CyberSafeEmerald.copy(alpha = 0.2f)
                                else CyberThreatRed.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRealtimeEnabled && isAccessibilityEnabled) "QALQON: FAOL" else "QALQON: O'CHIQ",
                            color = if (isRealtimeEnabled && isAccessibilityEnabled) CyberSafeEmerald else CyberThreatRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurfaceDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurfaceDark,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Boshqaruv") },
                    label = { Text("Boshqaruv", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_dashboard"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyberShieldCyan,
                        indicatorColor = CyberShieldCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                val threatsCount = storageApks.count { it.severity == ThreatSeverity.CRITICAL || it.severity == ThreatSeverity.HIGH }
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        if (storageApks.isEmpty()) viewModel.startStorageScan()
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "APK Skaner") },
                    label = {
                        Text(
                            text = if (threatsCount > 0) "Skaner ($threatsCount)" else "APK Skaner",
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.testTag("nav_scanner"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyberShieldCyan,
                        indicatorColor = CyberShieldCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        if (installedApps.isEmpty()) viewModel.startInstalledAppsScan()
                    },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Ilovalar") },
                    label = { Text("Ilovalar", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_installed"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyberShieldCyan,
                        indicatorColor = CyberShieldCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = "Kiber Lab") },
                    label = { Text("Kiber Lab", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_lab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyberShieldCyan,
                        indicatorColor = CyberShieldCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    status = overallStatus,
                    isRealtimeEnabled = isRealtimeEnabled,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    threatRecords = threatRecords,
                    totalApksCount = storageApks.size,
                    threatsFoundCount = storageApks.count { it.threatScore >= 35 },
                    onToggleRealtime = { viewModel.toggleRealtimeGuard(it) },
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onStartStorageScan = {
                        selectedTab = 1
                        viewModel.startStorageScan()
                    },
                    onStartInstalledAppsScan = {
                        selectedTab = 2
                        viewModel.startInstalledAppsScan()
                    },
                    onTriggerSimulation = { viewModel.triggerSimulatedThreatBlock() },
                    onNavigateToScanner = { selectedTab = 1 }
                )

                1 -> ApkScannerScreen(
                    apkList = storageApks,
                    isScanning = isScanning,
                    onStartScan = { viewModel.startStorageScan() },
                    onSelectApk = { viewModel.selectDetail(it) },
                    onDeleteApk = { viewModel.deleteApk(it) }
                )

                2 -> InstalledAppsScreen(
                    installedApps = installedApps,
                    isScanning = isScanning,
                    onStartScan = { viewModel.startInstalledAppsScan() },
                    onSelectApk = { viewModel.selectDetail(it) }
                )

                3 -> CyberLabScreen(
                    blacklistedHashes = blacklistedHashes,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    onAddBlacklistHash = { hash, name, cat, desc ->
                        viewModel.addBlacklistHash(hash, name, cat, desc)
                    },
                    onRemoveBlacklistHash = { viewModel.removeBlacklistHash(it) },
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onTriggerSimulation = { viewModel.triggerSimulatedThreatBlock() }
                )
            }

            // Detail Modal
            selectedDetail?.let { detail ->
                ThreatDetailDialog(
                    apkInfo = detail,
                    onDismiss = { viewModel.selectDetail(null) },
                    onDelete = if (!detail.isInstalled && detail.filePath != null) {
                        { viewModel.deleteApk(detail) }
                    } else null
                )
            }

            // Real-time Intercept Alert Modal
            activeAlertModal?.let { event ->
                SecurityAlertModal(
                    event = event,
                    onDismiss = { viewModel.dismissAlert() }
                )
            }
        }
    }
}
