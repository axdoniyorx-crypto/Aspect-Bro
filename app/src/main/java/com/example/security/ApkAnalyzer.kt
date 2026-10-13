package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.local.ThreatDatabase
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * ApkAnalyzer - APK fayllarini chuqur tahlil qiluvchi, Manifest ruxsatlarini tekshiruvchi,
 * SHA-256 xesh kodini oluvchi va tahdid darajasini aniqlovchi xavfsizlik dvigateli.
 */
class ApkAnalyzer(private val context: Context) {

    private val threatDao = ThreatDatabase.getInstance(context).threatDao()

    /**
     * Fayl tizimidagi .apk faylini chuqur tahlil qilish
     */
    suspend fun analyzeApkFile(file: File): ApkThreatInfo {
        if (!file.exists() || !file.canRead()) {
            return ApkThreatInfo(
                appName = file.name,
                packageName = "unknown.corrupt.file",
                versionName = "0.0",
                filePath = file.absolutePath,
                sha256 = "N/A",
                fileSizeBytes = file.length(),
                threatScore = 70,
                severity = ThreatSeverity.HIGH,
                threatReasons = listOf("Fayl o'qib bo'lmaydi yoki buzilgan APK"),
                isBlocked = true
            )
        }

        val sha256 = calculateSha256(file)
        val packageManager = context.packageManager

        // APK faylning Manifest va paket ma'lumotlarini o'qish
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    file.absolutePath,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            null
        }

        val appName = try {
            val appInfo = packageInfo?.applicationInfo
            if (appInfo != null) {
                appInfo.sourceDir = file.absolutePath
                appInfo.publicSourceDir = file.absolutePath
                packageManager.getApplicationLabel(appInfo).toString()
            } else {
                file.nameWithoutExtension
            }
        } catch (e: Exception) {
            file.nameWithoutExtension
        }

        val packageName = packageInfo?.packageName ?: "unknown.package"
        val versionName = packageInfo?.versionName ?: "1.0"
        val requestedPermissions = packageInfo?.requestedPermissions?.toList() ?: emptyList()

        return evaluateThreat(
            appName = appName,
            packageName = packageName,
            versionName = versionName,
            filePath = file.absolutePath,
            fileSize = file.length(),
            sha256 = sha256,
            permissions = requestedPermissions,
            isInstalled = false,
            isSystemApp = false
        )
    }

    /**
     * O'rnatilgan ilovani xavfsizlik auditidan o'tkazish
     */
    suspend fun analyzeInstalledApp(packageInfo: PackageInfo): ApkThreatInfo {
        val pm = context.packageManager
        val appInfo = packageInfo.applicationInfo
        val appName = try {
            if (appInfo != null) {
                pm.getApplicationLabel(appInfo).toString()
            } else {
                packageInfo.packageName ?: "Unknown"
            }
        } catch (e: Exception) {
            packageInfo.packageName ?: "Unknown"
        }

        val isSystem = if (appInfo != null) {
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } else {
            false
        }

        val sourceDir = appInfo?.sourceDir
        val apkFile = sourceDir?.let { File(it) }
        val fileSize = if (apkFile != null && apkFile.exists()) apkFile.length() else 0L
        val sha256 = if (apkFile != null && apkFile.exists()) calculateSha256(apkFile) else "N/A"
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        return evaluateThreat(
            appName = appName,
            packageName = packageInfo.packageName ?: "unknown.package",
            versionName = packageInfo.versionName ?: "1.0",
            filePath = sourceDir,
            fileSize = fileSize,
            sha256 = sha256,
            permissions = permissions,
            isInstalled = true,
            isSystemApp = isSystem
        )
    }

    /**
     * Xavf darajasini hisoblash algoritmi (Threat Scoring Engine)
     */
    private suspend fun evaluateThreat(
        appName: String,
        packageName: String,
        versionName: String,
        filePath: String?,
        fileSize: Long,
        sha256: String,
        permissions: List<String>,
        isInstalled: Boolean,
        isSystemApp: Boolean
    ): ApkThreatInfo {
        var score = 0
        val reasons = mutableListOf<String>()
        val dangerousList = mutableListOf<String>()

        // 1. Qora ro'yxat (Malware Hashes) tekshiruvi
        val blacklisted = threatDao.findBlacklistedByHash(sha256)
        if (blacklisted != null) {
            score = 100
            reasons.add("DIQQAT: Qora ro'yxatdagi xavfli virus (${blacklisted.threatName} - ${blacklisted.threatCategory})!")
            reasons.add(blacklisted.description)
            return ApkThreatInfo(
                appName = appName,
                packageName = packageName,
                versionName = versionName,
                filePath = filePath,
                sha256 = sha256,
                fileSizeBytes = fileSize,
                dangerousPermissions = permissions,
                totalPermissionsCount = permissions.size,
                threatScore = score,
                severity = ThreatSeverity.CRITICAL,
                threatReasons = reasons,
                isInstalled = isInstalled,
                isSystemApp = isSystemApp,
                isBlocked = true
            )
        }

        // Tizim ilovalarini alohida baholash
        if (isSystemApp) {
            return ApkThreatInfo(
                appName = appName,
                packageName = packageName,
                versionName = versionName,
                filePath = filePath,
                sha256 = sha256,
                fileSizeBytes = fileSize,
                dangerousPermissions = emptyList(),
                totalPermissionsCount = permissions.size,
                threatScore = 5,
                severity = ThreatSeverity.SAFE,
                threatReasons = listOf("Android tizimining rasmiy ishonchli ilovasi"),
                isInstalled = true,
                isSystemApp = true,
                isBlocked = false
            )
        }

        // 2. Xavfli ruxsatlar analizi
        for (perm in permissions) {
            when {
                perm.contains("BIND_ACCESSIBILITY_SERVICE", ignoreCase = true) -> {
                    score += 28
                    dangerousList.add("Maxsus imkoniyatlarni boshqarish (Accessibility Service)")
                    reasons.add("Ekranni to'liq o'qish va tugmalarni avtomatik bosish ruxsati")
                }
                perm.contains("SYSTEM_ALERT_WINDOW", ignoreCase = true) -> {
                    score += 18
                    dangerousList.add("Boshqa ilovalar ustida oyna ochish (Overlay)")
                    reasons.add("Bank ilovalari ustiga soxta kirish oynasi (phishing overlay) qo'yish xavfi")
                }
                perm.contains("REQUEST_INSTALL_PACKAGES", ignoreCase = true) -> {
                    score += 20
                    dangerousList.add("Ilovalarni yashirin o'rnatish (Dropper / Silent Install)")
                    reasons.add("Fondagi viruslarni avtomatik o'rnatish imkoniyati")
                }
                perm.contains("BIND_DEVICE_ADMIN", ignoreCase = true) -> {
                    score += 25
                    dangerousList.add("Qurilma administratori huquqi (Device Admin)")
                    reasons.add("Telefonni qulflash yoki o'chirish huquqi talab qilingan")
                }
                perm.contains("RECEIVE_SMS", ignoreCase = true) || perm.contains("READ_SMS", ignoreCase = true) -> {
                    score += 20
                    dangerousList.add("SMS xabarlarni o'qish va tutib olish (SMS Intercept)")
                    reasons.add("Bank va to'lov tizimlarining OTP kodlarini o'g'irlash xavfi")
                }
                perm.contains("SEND_SMS", ignoreCase = true) -> {
                    score += 15
                    dangerousList.add("SMS yuborish (Pullik xizmatlar xavfi)")
                }
                perm.contains("RECORD_AUDIO", ignoreCase = true) -> {
                    score += 10
                    dangerousList.add("Mikrofondan yashirin ovoz yozib olish (Spyware)")
                }
                perm.contains("CAMERA", ignoreCase = true) -> {
                    score += 8
                    dangerousList.add("Kamera ruxsati")
                }
                perm.contains("ACCESS_FINE_LOCATION", ignoreCase = true) -> {
                    score += 8
                    dangerousList.add("Aniq GPS joylashuvni kuzatish (Location Tracking)")
                }
                perm.contains("READ_CALL_LOG", ignoreCase = true) || perm.contains("PROCESS_OUTGOING_CALLS", ignoreCase = true) -> {
                    score += 12
                    dangerousList.add("Qo'ng'iroqlar tarixini o'qish (Spyware)")
                }
                perm.contains("READ_CONTACTS", ignoreCase = true) -> {
                    score += 10
                    dangerousList.add("Kontaktlar kitobini o'qish")
                }
            }
        }

        // 3. Shubhali xatti-harakatlar kombinatsiyasi (Heuristic Combo Analysis)
        val hasSms = permissions.any { it.contains("SMS", ignoreCase = true) }
        val hasOverlay = permissions.any { it.contains("SYSTEM_ALERT_WINDOW", ignoreCase = true) }
        val hasAccessibility = permissions.any { it.contains("ACCESSIBILITY", ignoreCase = true) }
        val hasDropper = permissions.any { it.contains("REQUEST_INSTALL_PACKAGES", ignoreCase = true) }
        val hasBoot = permissions.any { it.contains("RECEIVE_BOOT_COMPLETED", ignoreCase = true) }

        // Bank troyani kombinatsiyasi: SMS + Overlay yoki SMS + Accessibility
        if (hasSms && (hasOverlay || hasAccessibility)) {
            score += 30
            reasons.add("Kiberxavfsizlik signali: Bank troyani (Banking Trojan) xarakterli kombinatsiyasi aniqlandi!")
        }

        // Yashirin yuklovchi (Dropper): Ilova yuklash + Telefon yoqilganda ishga tushish
        if (hasDropper && hasBoot) {
            score += 20
            reasons.add("Heuristik ogohlantirish: Foniy avtomatik yuklovchi (Dropper) belgisi bor")
        }

        // Soxtalashtirish (Spoofing) tekshiruvi: Nomi mashhur, paketi shubhali
        val lowerName = appName.lowercase()
        val lowerPkg = packageName.lowercase()
        val suspiciousSpoof = (lowerName.contains("telegram") && !lowerPkg.contains("telegram")) ||
                (lowerName.contains("whatsapp") && !lowerPkg.contains("whatsapp")) ||
                (lowerName.contains("google") && !lowerPkg.contains("google")) ||
                (lowerName.contains("click") && !lowerPkg.contains("click")) ||
                (lowerName.contains("payme") && !lowerPkg.contains("payme")) ||
                (lowerName.contains("instagram") && !lowerPkg.contains("instagram"))

        if (suspiciousSpoof) {
            score += 35
            reasons.add("Soxta ilova (Phishing / Clone): Nomi mashhur servisdek, lekin paketi boshqa!")
        }

        // Ballni chegaralash (0-100)
        score = score.coerceIn(0, 100)

        val severity = when {
            score >= 80 -> ThreatSeverity.CRITICAL
            score >= 60 -> ThreatSeverity.HIGH
            score >= 35 -> ThreatSeverity.MEDIUM
            score >= 15 -> ThreatSeverity.LOW
            else -> ThreatSeverity.SAFE
        }

        if (reasons.isEmpty()) {
            reasons.add("Hech qanday xavfli xatti-harakat yoki shubhali ruxsat aniqlanmadi.")
        }

        val shouldBlock = severity == ThreatSeverity.CRITICAL || severity == ThreatSeverity.HIGH

        return ApkThreatInfo(
            appName = appName,
            packageName = packageName,
            versionName = versionName,
            filePath = filePath,
            sha256 = sha256,
            fileSizeBytes = fileSize,
            dangerousPermissions = dangerousList,
            totalPermissionsCount = permissions.size,
            threatScore = score,
            severity = severity,
            threatReasons = reasons,
            isInstalled = isInstalled,
            isSystemApp = isSystemApp,
            isBlocked = shouldBlock
        )
    }

    /**
     * Faylning SHA-256 xesh kodini hisoblash
     */
    fun calculateSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Noma'lum SHA-256 (${e.localizedMessage})"
        }
    }
}
