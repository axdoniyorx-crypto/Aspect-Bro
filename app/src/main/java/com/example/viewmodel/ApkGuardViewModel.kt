package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BlacklistHashEntity
import com.example.data.local.ThreatDatabase
import com.example.data.local.ThreatRecordEntity
import com.example.model.ApkThreatInfo
import com.example.model.ThreatSeverity
import com.example.scanner.ScanProgress
import com.example.scanner.StorageApkScanner
import com.example.security.ApkInstallGuardService
import com.example.security.InterceptEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SecurityStatus(val titleUz: String, val descriptionUz: String, val percentage: Int) {
    SECURE("Qurilma to'liq himoyalangan", "Real vaqtda APK o'rnatish qalqoni faol va tahdidlar yo'q", 98),
    WARNING("Ehtiyot bo'ling (Kuzatuv talab etiladi)", "Accessibility xizmatini yoqing yoki topilgan shubhali APK larni tekshiring", 65),
    DANGER("XAVF ANIQLANDI!", "Qurilmada xavfli yoki zararli APK fayllar topildi!", 20)
}

class ApkGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ThreatDatabase.getInstance(application)
    private val threatDao = db.threatDao()
    private val scanner = StorageApkScanner(application)

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _isRealtimeGuardEnabled = MutableStateFlow(true)
    val isRealtimeGuardEnabled: StateFlow<Boolean> = _isRealtimeGuardEnabled.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _storageApks = MutableStateFlow<List<ApkThreatInfo>>(emptyList())
    val storageApks: StateFlow<List<ApkThreatInfo>> = _storageApks.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ApkThreatInfo>>(emptyList())
    val installedApps: StateFlow<List<ApkThreatInfo>> = _installedApps.asStateFlow()

    private val _selectedDetail = MutableStateFlow<ApkThreatInfo?>(null)
    val selectedDetail: StateFlow<ApkThreatInfo?> = _selectedDetail.asStateFlow()

    private val _activeAlertModal = MutableStateFlow<InterceptEvent?>(null)
    val activeAlertModal: StateFlow<InterceptEvent?> = _activeAlertModal.asStateFlow()

    private val _messageSnackbar = MutableStateFlow<String?>(null)
    val messageSnackbar: StateFlow<String?> = _messageSnackbar.asStateFlow()

    val threatRecords: StateFlow<List<ThreatRecordEntity>> = threatDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklistedHashes: StateFlow<List<BlacklistHashEntity>> = threatDao.getAllBlacklistedHashes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshServiceStatus()
        // Dastlabki audit va demo ma'lumotlarni tayyorlash
        viewModelScope.launch {
            scanner.generateSandboxSampleApks()
            startStorageScan()
        }
    }

    fun refreshServiceStatus() {
        val enabled = ApkInstallGuardService.isAccessibilityServiceEnabled(getApplication())
        _isAccessibilityEnabled.value = enabled
    }

    fun toggleRealtimeGuard(enabled: Boolean) {
        _isRealtimeGuardEnabled.value = enabled
    }

    fun selectDetail(info: ApkThreatInfo?) {
        _selectedDetail.value = info
    }

    fun dismissAlert() {
        _activeAlertModal.value = null
    }

    fun clearSnackbar() {
        _messageSnackbar.value = null
    }

    /**
     * Xotiradagi barcha .apk fayllarni skanerlash
     */
    fun startStorageScan() {
        if (_isScanning.value) return
        _isScanning.value = true

        viewModelScope.launch {
            scanner.scanStorageApks().collect { (progress, items) ->
                _scanProgress.value = progress
                _storageApks.value = items

                if (progress.isFinished) {
                    _isScanning.value = false
                    _messageSnackbar.value = "Xotira skaneri tugallandi. ${items.size} ta APK tekshirildi."
                }
            }
        }
    }

    /**
     * O'rnatilgan ilovalarni auditdan o'tkazish
     */
    fun startInstalledAppsScan() {
        if (_isScanning.value) return
        _isScanning.value = true

        viewModelScope.launch {
            scanner.scanInstalledApps().collect { (progress, items) ->
                _scanProgress.value = progress
                _installedApps.value = items

                if (progress.isFinished) {
                    _isScanning.value = false
                    _messageSnackbar.value = "Ilovalar auditi yakunlandi. ${items.size} ta ilova tekshirildi."
                }
            }
        }
    }

    /**
     * Xavfli APK faylni xotiradan butunlay yo'q qilish
     */
    fun deleteApk(info: ApkThreatInfo) {
        val path = info.filePath ?: return
        viewModelScope.launch {
            val success = scanner.deleteApkFile(path)
            if (success) {
                _storageApks.value = _storageApks.value.filter { it.filePath != path }
                if (_selectedDetail.value?.filePath == path) {
                    _selectedDetail.value = null
                }
                _messageSnackbar.value = "${info.appName} fayli xotiradan muvaffaqiyatli o'chirildi!"
            } else {
                _messageSnackbar.value = "Faylni o'chirib bo'lmadi (xotira ruxsatini tekshiring)"
            }
        }
    }

    /**
     * Xavfli ilovalar bazasiga yangi SHA-256 xesh qo'shish
     */
    fun addBlacklistHash(hash: String, name: String, category: String, desc: String) {
        if (hash.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            threatDao.insertBlacklistHash(
                BlacklistHashEntity(
                    sha256 = hash.trim(),
                    threatName = name.trim(),
                    threatCategory = category.ifBlank { "Malware" },
                    description = desc.ifBlank { "Qo'lda qo'shilgan xavfli virus imzosi" }
                )
            )
            _messageSnackbar.value = "Yangi virus xeshi ($name) qora ro'yxatga qo'shildi!"
            // Qayta skanerlash
            startStorageScan()
        }
    }

    fun removeBlacklistHash(hash: String) {
        viewModelScope.launch {
            threatDao.deleteBlacklistHash(hash)
            _messageSnackbar.value = "Xesh qora ro'yxatdan olib tashlandi"
        }
    }

    /**
     * Kiberxavfsizlik simulyatsiyasi: Sinov tariqasida xavfli o'rnatish jarayoni
     * bloklanishini foydalanuvchiga amalda ko'rsatish
     */
    fun triggerSimulatedThreatBlock() {
        viewModelScope.launch {
            val event = InterceptEvent(
                appName = "Bank_Update_Cerberus_Demo.apk",
                packageName = "com.trojan.banker.cerberus.fake",
                blocked = true,
                reason = "DIQQAT: Qora ro'yxatdagi Trojan.AndroidOS.Cerberus aniqlandi! OTP va SMS o'g'irlash kodi bloklandi."
            )
            _activeAlertModal.value = event
            threatDao.insertRecord(
                ThreatRecordEntity(
                    appName = event.appName,
                    packageName = event.packageName,
                    sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    threatScore = 100,
                    severity = ThreatSeverity.CRITICAL.name,
                    reasonsSummary = event.reason,
                    filePath = "/sdcard/Download/Bank_Update_Cerberus_Demo.apk",
                    wasBlocked = true
                )
            )
        }
    }

    fun getOverallStatus(): SecurityStatus {
        val hasCritical = _storageApks.value.any { it.severity == ThreatSeverity.CRITICAL } ||
                _installedApps.value.any { it.severity == ThreatSeverity.CRITICAL }
        val hasHigh = _storageApks.value.any { it.severity == ThreatSeverity.HIGH }

        return when {
            hasCritical || hasHigh -> SecurityStatus.DANGER
            !_isAccessibilityEnabled.value || !_isRealtimeGuardEnabled.value -> SecurityStatus.WARNING
            else -> SecurityStatus.SECURE
        }
    }
}
