package com.dcs.core.model

/**
 * Ephemeral data extracted from a notification.
 * Used only for processing — never persisted to storage.
 */
data class NotificationData(
    val packageName: String,
    val title: String?,
    val messageText: String?,
    val timestamp: Long
)
