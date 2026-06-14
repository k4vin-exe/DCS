package com.dcs.core.constants

/**
 * Application-wide constants for Digital Communication Safeguard.
 * All threshold values and configuration are centralized here.
 */
object AppConstants {

    /** Package names of messaging apps to monitor via NotificationListenerService */
    val MONITORED_PACKAGES = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "org.thoughtcrime.securesms",       // Signal
        "com.facebook.orca",                 // Messenger
        "com.instagram.android",
        "com.google.android.apps.messaging", // Google Messages
        "com.android.mms",                   // Default Android SMS
        "com.samsung.android.messaging"      // Samsung Messages
    )

    /** Human-readable app names for UI display */
    val APP_NAMES = mapOf(
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "org.telegram.messenger" to "Telegram",
        "org.thoughtcrime.securesms" to "Signal",
        "com.facebook.orca" to "Messenger",
        "com.instagram.android" to "Instagram",
        "com.google.android.apps.messaging" to "Messages",
        "com.android.mms" to "SMS",
        "com.samsung.android.messaging" to "Samsung Messages"
    )

    // Risk Score Thresholds
    const val RISK_THRESHOLD_SAFE = 30
    const val RISK_THRESHOLD_SUSPICIOUS = 70

    // Fusion Weights
    const val RULE_WEIGHT = 0.6f
    const val ML_WEIGHT = 0.4f

    // Overlay Timing
    const val OVERLAY_DISPLAY_DURATION_MS = 10_000L

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "dcs_threat_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Threat Alerts"
    const val OVERLAY_SERVICE_NOTIFICATION_ID = 1001
    const val THREAT_NOTIFICATION_ID = 2001
}
