package com.dcs.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dcs.core.model.RiskLevel
import com.dcs.core.model.ThreatType
import com.dcs.presentation.theme.DCSTheme
import com.dcs.presentation.theme.DangerRed
import com.dcs.presentation.theme.DangerRedDark
import com.dcs.presentation.theme.WarningOrange
import com.dcs.presentation.theme.WarningOrangeDark
import kotlinx.coroutines.delay

/**
 * Premium overlay component drawn via SYSTEM_ALERT_WINDOW.
 * Features glassmorphism-style borders, vivid threat colors, and entry/exit animations.
 */
@Composable
fun ThreatAlertOverlay(
    riskLevel: RiskLevel,
    threatType: ThreatType,
    sourceApp: String,
    reasons: List<String>,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Trigger entry animation
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Determine visual style based on threat severity
    val isDangerous = riskLevel == RiskLevel.DANGEROUS
    val mainColor = if (isDangerous) DangerRed else WarningOrange
    val bgColor = if (isDangerous) DangerRedDark.copy(alpha = 0.95f) else WarningOrangeDark.copy(alpha = 0.95f)
    val icon = if (isDangerous) Icons.Default.GppBad else Icons.Default.WarningAmber
    val title = if (isDangerous) "CRITICAL THREAT BLOCKED" else "SUSPICIOUS ACTIVITY"

    DCSTheme(darkTheme = true) { // Always dark theme for overlay to stand out
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 400)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, mainColor, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Alert",
                                    tint = mainColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        isVisible = false
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content
                        Text(
                            text = "DCS detected a ${threatType.label.lowercase()} attempt via $sourceApp.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )

                        if (reasons.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    reasons.take(3).forEach { reason ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "• ",
                                                color = mainColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = reason,
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Do not share personal info or OTPs.",
                            color = mainColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Handle dismissal logic after animation
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300) // Wait for exit animation
            onDismiss()
        }
    }
}
