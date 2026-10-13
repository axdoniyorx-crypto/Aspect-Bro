package com.example.scanner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import com.example.security.ApkAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ScanProgress(
    val currentItemName: String = "",
    val scannedCount: Int = 0,
    val totalFound: Int = 0,
    val threatCount: Int = 0,
    val latestThreat: ApkThreatInfo? = null,
    val isFinished: Boolean = false
)

/**
 * StorageApkScanner - Qurilma xotirasidan .apk fayllarini qidirish,
 * ularni to'liq skanerlash va o'rnatilgan ilovalarni audit qilish klassi.
 */
class StorageApkScanner(private val context: Context) {

    private val analyzer = ApkAnalyzer(context)

    /**
     * Telefon xotirasidagi barcha .apk fayllarni topish va tahlil qilish (Flow orqali)
     */
    fun scanStorageApks(): Flow<Pair<ScanProgress, List<ApkThreatInfo>>> = flow {
        val foundApks = findApkFilesInStorage()
        val results = mutableListOf<ApkThreatInfo>()
        var threatsCount = 0

        if (foundApks.isEmpty()) {
            emit(
                Pair(
                    ScanProgress(
                        currentItemName = "Hech qanday APK topilmadi",
                        scannedCount = 0,
                        totalFound = 0,
                        threatCount = 0,
                        isFinished = true
                    ),
                    emptyList()
                )
            )
            return@flow
        }

        foundApks.forEachIndexed { index, apkFile ->
            val threatInfo = analyzer.analyzeApkFile(apkFile)
            results.add(threatInfo)

            if (threatInfo.severity == ThreatSeverity.CRITICAL ||
                threatInfo.severity == ThreatSeverity.HIGH ||
                threatInfo.severity == ThreatSeverity.MEDIUM
            ) {
                threatsCount++
            }

            emit(
                Pair(
                    ScanProgress(
                        currentItemName = apkFile.name,
                        scannedCount = index + 1,
                        totalFound = foundApks.size,
                        threatCount = threatsCount,
                        latestThreat = threatInfo,
                        isFinished = index == foundApks.size - 1
                    ),
                    results.toList()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Qurilmaga o'rnatilgan barcha ilovalarni auditdan o'tkazish
     */
    fun scanInstalledApps(): Flow<Pair<ScanProgress, List<ApkThreatInfo>>> = flow {
        val pm = context.packageManager
        val packages = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val results = mutableListOf<ApkThreatInfo>()
        var threatsCount = 0

        packages.forEachIndexed { index, pkgInfo ->
            val threat = analyzer.analyzeInstalledApp(pkgInfo)
            results.add(threat)

            if (threat.severity == ThreatSeverity.CRITICAL ||
                threat.severity == ThreatSeverity.HIGH ||
                threat.severity == ThreatSeverity.MEDIUM
            ) {
                threatsCount++
            }

            emit(
                Pair(
                    ScanProgress(
                        currentItemName = threat.appName,
                        scannedCount = index + 1,
                        totalFound = packages.size,
                        threatCount = threatsCount,
                        latestThreat = threat,
                        isFinished = index == packages.size - 1
                    ),
                    results.toList()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Xotiradan APK fayllarni qidirish (Downloads, Telegram, Documents, Storage)
     */
    fun findApkFilesInStorage(): List<File> {
        val apkList = mutableListOf<File>()

        val searchDirs = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            File(Environment.getExternalStorageDirectory(), "Telegram"),
            File(Environment.getExternalStorageDirectory(), "WhatsApp/Media"),
            context.getExternalFilesDir(null)
        )

        for (dir in searchDirs) {
            if (dir != null && dir.exists() && dir.canRead()) {
                searchDirectoryForApks(dir, apkList, maxDepth = 2)
            }
        }

        // Shuningdek, ilovaning o'z kesh/sandbox papkasidagi simulyatsiya APK lari
        val sandboxDir = File(context.filesDir, "sandbox_apks")
        if (sandboxDir.exists()) {
            searchDirectoryForApks(sandboxDir, apkList, maxDepth = 1)
        }

        return apkList.distinctBy { it.absolutePath }
    }

    private fun searchDirectoryForApks(dir: File, result: MutableList<File>, maxDepth: Int) {
        if (maxDepth <= 0) return
        val files = dir.listFiles() ?: return

        for (f in files) {
            if (f.isFile && f.name.endsWith(".apk", ignoreCase = true)) {
                result.add(f)
            } else if (f.isDirectory && !f.name.startsWith(".")) {
                searchDirectoryForApks(f, result, maxDepth - 1)
            }
        }
    }

    /**
     * Xavfli deb topilgan APK faylni xotiradan to'liq o'chirib tashlash
     */
    suspend fun deleteApkFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Xavfsizlik testi (Sandbox Demo): Foydalanuvchi sinab ko'rishi uchun
     * xavfli va toza test APK fayllarini yaratish imkoniyati
     */
    suspend fun generateSandboxSampleApks(): Boolean = withContext(Dispatchers.IO) {
        try {
            val sandboxDir = File(context.filesDir, "sandbox_apks")
            if (!sandboxDir.exists()) sandboxDir.mkdirs()

            // 1. Simulyatsiya qilingan Zararli APK (Cerberus Bank Trojan belgilariga ega)
            val fakeMalware = File(sandboxDir, "Bank_Update_Cerberus_Demo.apk")
            if (!fakeMalware.exists()) {
                FileOutputStream(fakeMalware).use { fos ->
                    fos.write("DEMO_MALWARE_APK_SIGNATURE_BANK_SMS_OVERLAY".toByteArray())
                }
            }

            // 2. Simulyatsiya qilingan Toza APK
            val fakeClean = File(sandboxDir, "Calculator_Safe_App.apk")
            if (!fakeClean.exists()) {
                FileOutputStream(fakeClean).use { fos ->
                    fos.write("DEMO_SAFE_APP_SIGNATURE_STANDARD_MATH".toByteArray())
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
