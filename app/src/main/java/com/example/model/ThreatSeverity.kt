package com.example.model

enum class ThreatSeverity(val labelUz: String, val level: Int) {
    SAFE("Xavfsiz (Safe)", 0),
    LOW("Past xavf (Low)", 1),
    MEDIUM("O'rta xavf (Medium)", 2),
    HIGH("Yuqori xavf (High)", 3),
    CRITICAL("O'ta xavfli (Critical / Malware)", 4)
}
