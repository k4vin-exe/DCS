package com.dcs.notification.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dcs.core.constants.AppConstants
import com.dcs.core.model.RiskLevel
import com.dcs.core.model.ThreatResult
import com.dcs.core.model.ThreatType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service responsible for drawing a SYSTEM_ALERT_WINDOW overlay
 * when a threat is detected. Uses Jetpack Compose for the UI.
 */
@AndroidEntryPoint
class ThreatOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    
    // Using a simple lifecycle owner for the Compose view in a Service
    private val lifecycleOwner = OverlayLifecycleOwner()
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val EXTRA_RISK_LEVEL = "extra_risk_level"
        private const val EXTRA_THREAT_TYPE = "extra_threat_type"
        private const val EXTRA_SOURCE_APP = "extra_source_app"
        private const val EXTRA_REASONS = "extra_reasons"

        fun showOverlay(context: Context, threatResult: ThreatResult) {
            val intent = Intent(context, ThreatOverlayService::class.java).apply {
                putExtra(EXTRA_RISK_LEVEL, threatResult.riskLevel.name)
                putExtra(EXTRA_THREAT_TYPE, threatResult.threatType.name)
                putExtra(EXTRA_SOURCE_APP, threatResult.sourceApp)
                putExtra(EXTRA_REASONS, threatResult.reasons.toTypedArray())
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If starting the foreground service fails (e.g. background restrictions),
                // we gracefully ignore it to prevent the app from crashing.
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                AppConstants.OVERLAY_SERVICE_NOTIFICATION_ID,
                createForegroundNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                AppConstants.OVERLAY_SERVICE_NOTIFICATION_ID,
                createForegroundNotification()
            )
        }
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val riskLevel = RiskLevel.valueOf(intent.getStringExtra(EXTRA_RISK_LEVEL) ?: RiskLevel.SAFE.name)
        val threatType = ThreatType.valueOf(intent.getStringExtra(EXTRA_THREAT_TYPE) ?: ThreatType.NONE.name)
        val sourceApp = intent.getStringExtra(EXTRA_SOURCE_APP) ?: "Unknown App"
        val reasons = intent.getStringArrayExtra(EXTRA_REASONS)?.toList() ?: emptyList()

        showComposeOverlay(riskLevel, threatType, sourceApp, reasons)

        // Auto-dismiss after 10 seconds
        serviceScope.launch {
            delay(AppConstants.OVERLAY_DISPLAY_DURATION_MS)
            removeOverlayAndStop()
        }

        return START_NOT_STICKY
    }

    private fun showComposeOverlay(
        riskLevel: RiskLevel,
        threatType: ThreatType,
        sourceApp: String,
        reasons: List<String>
    ) {
        // Remove existing view if one is already showing
        removeOverlay()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100 // Slight offset from top
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            // Here we provide a minimal setup
            setContent {
                // We will implement this composable in the presentation module
                // For now, we use a placeholder or import it once presentation is built
                com.dcs.presentation.component.ThreatAlertOverlay(
                    riskLevel = riskLevel,
                    threatType = threatType,
                    sourceApp = AppConstants.APP_NAMES[sourceApp] ?: sourceApp,
                    reasons = reasons,
                    onDismiss = { removeOverlayAndStop() }
                )
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun removeOverlay() {
        composeView?.let {
            if (it.isAttachedToWindow) {
                windowManager.removeView(it)
            }
            composeView = null
        }
    }

    private fun removeOverlayAndStop() {
        removeOverlay()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                AppConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification() = NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_ID)
        .setContentTitle("DCS Security Overlay Active")
        .setContentText("Displaying threat alert over other apps.")
        .setSmallIcon(android.R.drawable.ic_dialog_alert) // Placeholder icon
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}
