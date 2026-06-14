package com.dcs.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dcs.presentation.dashboard.DashboardScreen
import com.dcs.presentation.history.ThreatHistoryScreen
import com.dcs.presentation.settings.SettingsScreen
import com.dcs.presentation.theme.DCSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        
        setContent {
            DCSTheme {
                DcsAppNavHost()
            }
            
            // Check for necessary permissions on launch
            LaunchedEffect(Unit) {
                checkPermissions()
            }
        }
    }

    private fun checkPermissions() {
        // 1. Notification Listener Permission
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }

        // 2. System Alert Window Permission (for overlays)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }
}

@Composable
fun DcsAppNavHost() {
    val navController = rememberNavController()
    
    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("history") {
                ThreatHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
