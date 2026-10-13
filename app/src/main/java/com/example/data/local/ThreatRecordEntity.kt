package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_records")
data class ThreatRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val sha256: String,
    val threatScore: Int,
    val severity: String,
    val reasonsSummary: String,
    val filePath: String?,
    val wasBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blacklisted_hashes")
data class BlacklistHashEntity(
    @PrimaryKey
    val sha256: String,
    val threatName: String,
    val threatCategory: String, // e.g. "Banking Trojan", "SMS Stealer", "Spyware", "Ransomware"
    val description: String,
    val dateAdded: Long = System.currentTimeMillis()
)
