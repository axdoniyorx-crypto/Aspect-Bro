package com.example.security

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.ThreatDatabase
import com.example.data.local.ThreatRecordEntity
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class InterceptEvent(
    val appName: String,
    val packageName: String,
    val blocked: Boolean,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ApkInstallGuardService - Real vaqtda tizimdagi Package Installer darchalarini
 * ushlab qoluvchi (intercept qiluvchi) va zararli ilovalarni avtomatik tarzda
 * 'Bekor qilish' (Cancel) orqali bloklovchi maxsus servis.
 */
class ApkInstallGuardService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var analyzer: ApkAnalyzer

    companion object {
        private const val TAG = "ApkInstallGuard"
        private const val CHANNEL_ID = "apk_soqchi_alerts"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private val _lastIntercepted = MutableStateFlow<InterceptEvent?>(null)
        val lastIntercepted = _lastIntercepted.asStateFlow()

        private val _totalBlockedCount = MutableStateFlow(0)
        val totalBlockedCount = _totalBlockedCount.asStateFlow()

        /**
         * Tizim parametrlarida ushbu Accessibility Servis yoqilganligini tekshirish
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${ApkInstallGuardService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(":").any { it.equals(expectedServiceName, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true
        analyzer = ApkAnalyzer(applicationContext)
        createNotificationChannel()
        Log.i(TAG, "Apk Soqchi Accessibility Himoya Xizmati faollashtirildi!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return

        // Turli brendlardagi o'rnatish paketlari (Google, Samsung, Xiaomi, Huawei, AOSP va h.k.)
        val isInstaller = pkgName.contains("packageinstaller", ignoreCase = true) ||
                pkgName.contains("installer", ignoreCase = true) ||
                pkgName == "com.google.android.packageinstaller" ||
                pkgName == "com.android.packageinstaller"

        if (!isInstaller) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val rootNode = rootInActiveWindow ?: return
            handleInstallerWindow(rootNode, pkgName)
        }
    }

    /**
     * O'rnatish darchasini o'qish, tahlil qilish va kerak bo'lsa darhol bloklash
     */
    private fun handleInstallerWindow(rootNode: AccessibilityNodeInfo, installerPackage: String) {
        val detectedTexts = mutableListOf<String>()
        extractAllText(rootNode, detectedTexts)

        val joinedText = detectedTexts.joinToString(" ")
        Log.d(TAG, "Installer darchasi aniqlandi: $joinedText")

        // O'rnatilayotgan ilova nomini matn ichidan izlash
        val appNameCandidate = detectedTexts.firstOrNull { it.length in 3..40 && !it.contains("o'rnat", ignoreCase = true) && !it.contains("install", ignoreCase = true) }
            ?: "Noma'lum APK"

        // Downloads papkasidagi eng so'nggi APK fayllarni tekshirish
        serviceScope.launch {
            val dangerousApk = findRecentlyDownloadedSuspiciousApk()

            if (dangerousApk != null) {
                // Xavfli deb topildi! Darhol bloklash mexanizmini ishga tushirish!
                val successBlocked = performStrictBlock(rootNode)
                if (successBlocked) {
                    _totalBlockedCount.value += 1
                    val intercept = InterceptEvent(
                        appName = dangerousApk.appName.ifBlank { appNameCandidate },
                        packageName = dangerousApk.packageName,
                        blocked = true,
                        reason = dangerousApk.threatReasons.firstOrNull() ?: "Zararli ruxsatlar yoki xavfli kod aniqlandi!"
                    )
                    _lastIntercepted.value = intercept

                    // Room bazaga yozish
                    val db = ThreatDatabase.getInstance(applicationContext)
                    db.threatDao().insertRecord(
                        ThreatRecordEntity(
                            appName = intercept.appName,
                            packageName = intercept.packageName,
                            sha256 = dangerousApk.sha256,
                            threatScore = dangerousApk.threatScore,
                            severity = dangerousApk.severity.name,
                            reasonsSummary = intercept.reason,
                            filePath = dangerousApk.filePath,
                            wasBlocked = true
                        )
                    )

                    // Xavfsizlik signali xabarnomasini chiqarish
                    showThreatBlockedNotification(intercept)
                }
            }
        }
    }

    /**
     * O'rnatish darchasidagi barcha matnlarni yig'ib olish
     */
    private fun extractAllText(node: AccessibilityNodeInfo?, result: MutableList<String>) {
        if (node == null) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { result.add(it.trim()) }
        for (i in 0 until node.childCount) {
            extractAllText(node.getChild(i), result)
        }
    }

    /**
     * Qat'iy Bloklash (Strict Block): "Cancel" / "Bekor qilish" / "Отмена" tugmasini bosish
     * yoki GLOBAL_ACTION_BACK orqali darchani yopib tashlash
     */
    private fun performStrictBlock(rootNode: AccessibilityNodeInfo): Boolean {
        // 1-usul: Tugmalar matnini izlash ("Bekor qilish", "Cancel", "Отмена", "Bekor")
        val cancelLabels = listOf(
            "Bekor qilish", "Bekor", "Cancel", "Отмена", "Отменить",
            "Yopish", "Close", "Rad etish", "Deny", "Reject"
        )

        for (label in cancelLabels) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(label)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (node.isClickable) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.i(TAG, "Xavfli APK o'rnatilishi tugmani bosish orqali BLOKLANDI! (Tugma: $label)")
                        return true
                    }
                    val parent = node.parent
                    if (parent != null && parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.i(TAG, "Xavfli APK o'rnatilishi ota-tugma orqali BLOKLANDI!")
                        return true
                    }
                }
            }
        }

        // 2-usul: Maxsus tugma ID larini izlash (cancel_button, button2)
        val buttonIds = listOf(
            "com.android.packageinstaller:id/cancel_button",
            "com.google.android.packageinstaller:id/cancel_button",
            "android:id/button2"
        )
        for (id in buttonIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.i(TAG, "Xavfli APK o'rnatilishi ID bo'yicha BLOKLANDI!")
                return true
            }
        }

        // 3-usul: Agar tugma topilmasa, orqaga (GLOBAL_ACTION_BACK) orqali darchani darhol yopish!
        val backSuccess = performGlobalAction(GLOBAL_ACTION_BACK)
        if (backSuccess) {
            Log.i(TAG, "Xavfli APK darchasi GLOBAL_ACTION_BACK orqali yopildi!")
            return true
        }

        return false
    }

    /**
     * Eng so'nggi yuklab olingan APK faylini tekshirish
     */
    private suspend fun findRecentlyDownloadedSuspiciousApk(): ApkThreatInfo? {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadDir.exists() || !downloadDir.canRead()) return null

        val apkFiles = downloadDir.listFiles { file ->
            file.isFile && file.name.endsWith(".apk", ignoreCase = true)
        } ?: return null

        // Eng so'nggi 5 daqiqa ichida o'zgartirilgan yoki yuklangan fayl
        val latestApk = apkFiles.maxByOrNull { it.lastModified() } ?: return null
        val ageMillis = System.currentTimeMillis() - latestApk.lastModified()
        if (ageMillis > 10 * 60 * 1000) { // 10 daqiqadan eski bo'lsa tekshirmaymiz
            return null
        }

        val threat = analyzer.analyzeApkFile(latestApk)
        return if (threat.severity == ThreatSeverity.CRITICAL || threat.severity == ThreatSeverity.HIGH) {
            threat
        } else {
            null
        }
    }

    private fun showThreatBlockedNotification(event: InterceptEvent) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setContentTitle("🛡️ XAVF BLOKLANDI! (${event.appName})")
            .setContentText("Zararli APK o'rnatilishi to'xtatildi: ${event.reason}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Apk Soqchi qalqoni ${event.appName} o'rnatilishini avtomatik bekor qildi.\nSabab: ${event.reason}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Apk Soqchi Xavfsizlik Signallari",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Xavfli APK o'rnatilishi bloklanganda beriladigan ogohlantirishlar"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Apk Soqchi xizmati to'xtatildi")
        _isServiceRunning.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
    }
}
