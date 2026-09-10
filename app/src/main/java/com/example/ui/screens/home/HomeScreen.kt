package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnomalyInfo
import com.example.data.model.AppUsageItem
import com.example.data.model.DataHealthScore
import com.example.data.model.DataPlan
import com.example.data.model.DataRunwayInfo
import com.example.data.model.RunwayStatus
import com.example.ui.components.AppUsageRow
import com.example.ui.components.PermissionWarningBanner
import com.example.ui.components.StatusPill
import com.example.ui.theme.ColorCritical
import com.example.ui.theme.ColorInfo
import com.example.ui.theme.ColorSafe
import com.example.ui.theme.ColorWarning
import com.example.util.DataFormatUtils

@Composable
fun HomeScreen(
    dataPlan: DataPlan?,
    runwayInfo: DataRunwayInfo,
    todayUsageBytes: Long,
    normalDailyUsageBytes: Long,
    topApps: List<AppUsageItem>,
    anomalies: List<AnomalyInfo>,
    healthScore: DataHealthScore,
    hasUsagePermission: Boolean,
    onGrantPermissionClick: () -> Unit,
    onAppClick: (AppUsageItem) -> Unit,
    onSeeAllAppsClick: () -> Unit,
    onAddPlanClick: () -> Unit,
    onWhatAteMyDataClick: () -> Unit,
    onOptimizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val carrier = dataPlan?.networkCarrier ?: "MTN"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DATA GUARD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$carrier • Mobile Data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val (statusText, statusColor) = when (runwayInfo.status) {
                    RunwayStatus.ON_TRACK -> Pair("On track", ColorSafe)
                    RunwayStatus.LEARNING -> Pair("Learning", ColorInfo)
                    RunwayStatus.FASTER_THAN_USUAL -> Pair("High pace", ColorWarning)
                    RunwayStatus.CRITICAL_SHORTAGE -> Pair("Runway risk", ColorCritical)
                    RunwayStatus.NO_PLAN -> Pair("No plan", MaterialTheme.colorScheme.outline)
                }
                StatusPill(text = statusText, color = statusColor)
            }
        }

        // Permission Banner if required
        if (!hasUsagePermission) {
            item {
                PermissionWarningBanner(onGrantClick = onGrantPermissionClick)
            }
        }

        // HERO REMAINING CARD (or Plan Setup if no plan)
        if (dataPlan == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_plan_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "NO PLAN CONFIGURED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Set plan size & renewal date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = onAddPlanClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_data_plan_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Plan")
                        }
                    }
                }
            }
        } else {
            item {
                HeroRemainingCard(
                    runwayInfo = runwayInfo
                )
            }
        }

        // TODAY CARD
        item {
            TodayCard(
                todayBytes = todayUsageBytes,
                normalBytes = normalDailyUsageBytes,
                status = runwayInfo.status
            )
        }

        // WHAT'S USING YOUR DATA? CARD (Top 3 Apps + Background Warning + STOP IT)
        item {
            WhatsUsingYourDataCard(
                topApps = topApps.take(3),
                anomalies = anomalies,
                onAppClick = onAppClick,
                onStopClick = onOptimizeClick,
                onSeeAllClick = onSeeAllAppsClick
            )
        }

        // DATA RUNWAY CARD
        if (dataPlan != null) {
            item {
                DataRunwayCard(
                    runwayInfo = runwayInfo,
                    onOptimizeClick = onOptimizeClick
                )
            }
        }

        // 🔎 WHAT ATE MY DATA? (Primary Action)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onWhatAteMyDataClick)
                    .testTag("what_ate_my_data_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "WHAT ATE MY DATA?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Instant detective report",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroRemainingCard(
    runwayInfo: DataRunwayInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_data_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DataFormatUtils.formatBytes(runwayInfo.remainingBytes),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Progress Bar
            val total = runwayInfo.planSizeBytes.coerceAtLeast(1L)
            val progress = (runwayInfo.usedBytesInPeriod.toFloat() / total.toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }

            Text(
                text = "${DataFormatUtils.formatBytes(runwayInfo.usedBytesInPeriod)} used • ${runwayInfo.daysRemaining} days left",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayCard(
    todayBytes: Long,
    normalBytes: Long,
    status: RunwayStatus
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_usage_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )

                if (status == RunwayStatus.LEARNING) {
                    Text(
                        text = "Learning",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorInfo
                    )
                } else if (normalBytes > 0) {
                    val isAbove = todayBytes > normalBytes
                    val pct = DataFormatUtils.formatPercentageDiff(todayBytes, normalBytes)
                    val statusText = if (isAbove) "🔴 $pct higher" else "🟢 On track"
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAbove) ColorCritical else ColorSafe
                    )
                }
            }

            Text(
                text = DataFormatUtils.formatBytes(todayBytes),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (normalBytes > 0 && status != RunwayStatus.LEARNING) {
                Text(
                    text = "Normal: ${DataFormatUtils.formatBytes(normalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WhatsUsingYourDataCard(
    topApps: List<AppUsageItem>,
    anomalies: List<AnomalyInfo>,
    onAppClick: (AppUsageItem) -> Unit,
    onStopClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WHAT'S USING YOUR DATA?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onSeeAllClick)
                        .testTag("see_all_apps_button")
                )
            }

            // Top 3 Apps
            topApps.forEach { app ->
                AppUsageRow(
                    app = app,
                    onClick = { onAppClick(app) },
                    showWifi = false
                )
            }

            // Compact Background Warning (if an app has background drain or anomaly)
            val bgAnomaly = anomalies.firstOrNull { it.isBackgroundDominant }
            val topBgApp = topApps.firstOrNull { it.backgroundBytes > 30L * 1024 * 1024 }
            val warningTarget = bgAnomaly?.appName ?: topBgApp?.appName
            val warningBytes = bgAnomaly?.backgroundBytes ?: topBgApp?.backgroundBytes

            if (warningTarget != null && warningBytes != null && warningBytes > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorCritical.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ColorCritical,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "⚠️ $warningTarget",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorCritical
                                )
                                Text(
                                    text = "${DataFormatUtils.formatBytes(warningBytes)} • Background",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onStopClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorCritical
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("stop_it_button")
                        ) {
                            Text(
                                text = "STOP IT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRunwayCard(
    runwayInfo: DataRunwayInfo,
    onOptimizeClick: () -> Unit
) {
    val isShort = runwayInfo.expectedShortageDays > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("runway_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATA RUNWAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )

                val statusText = if (isShort) "🔴 ${runwayInfo.expectedShortageDays} days short" else "🟢 You're on track"
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isShort) ColorCritical else ColorSafe
                )
            }

            Text(
                text = "${runwayInfo.runwayDaysEstimate.toInt()} days",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isShort) ColorCritical else MaterialTheme.colorScheme.onSurface
            )

            // Visual Runway Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalDays = (runwayInfo.runwayDaysEstimate.toInt() + runwayInfo.daysRemaining).coerceAtLeast(1)
                val runwayFrac = (runwayInfo.runwayDaysEstimate.toFloat() / totalDays.toFloat()).coerceIn(0.1f, 1f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(runwayFrac)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (isShort) ColorCritical else ColorSafe)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onOptimizeClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("optimize_my_data_button")
                ) {
                    Text(
                        text = "OPTIMIZE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
