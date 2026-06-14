package com.dcs.core.model

/** Types of threats the system can detect. */
enum class ThreatType(val label: String) {
    SCAM("Scam"),
    PHISHING("Phishing"),
    ABUSE("Abuse"),
    HARASSMENT("Harassment"),
    EMOTIONAL_MANIPULATION("Emotional Manipulation"),
    THREAT("Threat"),
    NONE("None")
}
