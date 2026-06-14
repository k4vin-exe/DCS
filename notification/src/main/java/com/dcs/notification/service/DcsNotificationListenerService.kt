package com.dcs.notification.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dcs.core.constants.AppConstants
import com.dcs.core.model.NotificationData
import com.dcs.core.model.RiskLevel
import com.dcs.domain.repository.SettingsRepository
import com.dcs.domain.usecase.AnalyzeMessageUseCase
import com.dcs.notification.overlay.ThreatOverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Intercepts incoming notifications from targeted messaging apps.
 * Extracts ephemeral data, passes it to the ML analysis pipeline, and triggers overlays if needed.
 */
@AndroidEntryPoint
class DcsNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var analyzeMessageUseCase: AnalyzeMessageUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (sbn == null) return
        
        val packageName = sbn.packageName
        
        // Allow all apps during testing to ensure the flow works
        // if (!AppConstants.MONITORED_PACKAGES.contains(packageName)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        // Ignore empty messages
        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        // We only process if scanning is enabled globally
        serviceScope.launch {
            if (!settingsRepository.isScanningEnabled().first()) return@launch

            val notificationData = NotificationData(
                packageName = packageName,
                title = title,
                messageText = text,
                timestamp = sbn.postTime
            )

            // Analyze the ephemeral notification data
            val result = analyzeMessageUseCase(notificationData)

            // If the risk is elevated and overlays are enabled, show the alert
            if (result.riskLevel == RiskLevel.SUSPICIOUS || result.riskLevel == RiskLevel.DANGEROUS) {
                if (settingsRepository.isOverlayEnabled().first()) {
                    ThreatOverlayService.showOverlay(
                        context = applicationContext,
                        threatResult = result
                    )
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // No action needed on removal
    }
}
