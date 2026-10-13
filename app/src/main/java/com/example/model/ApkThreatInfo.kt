package com.example.model

data class ApkThreatInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val filePath: String? = null,
    val sha256: String,
    val fileSizeBytes: Long = 0L,
    val dangerousPermissions: List<String> = emptyList(),
    val totalPermissionsCount: Int = 0,
    val threatScore: Int = 0, // 0 - 100
    val severity: ThreatSeverity = ThreatSeverity.SAFE,
    val threatReasons: List<String> = emptyList(),
    val isInstalled: Boolean = false,
    val isSystemApp: Boolean = false,
    val isBlocked: Boolean = false,
    val scanTimestamp: Long = System.currentTimeMillis()
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "Noma'lum"
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb < 1.0) {
                "${fileSizeBytes / 1024} KB"
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
