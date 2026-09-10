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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import java.util.Calendar

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
    val greeting = rememberGreeting()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (dataPlan != null) "${dataPlan.networkCarrier} • ${dataPlan.planName}" else "Mobile Data Intelligence",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Global Status Pill
                val (statusText, statusColor) = when (runwayInfo.status) {
                    RunwayStatus.ON_TRACK -> Pair("On Track", ColorSafe)
                    RunwayStatus.LEARNING -> Pair("Learning", ColorInfo)
                    RunwayStatus.FASTER_THAN_USUAL -> Pair("High Pace", ColorWarning)
                    RunwayStatus.CRITICAL_SHORTAGE -> Pair("Runway Risk", ColorCritical)
                    RunwayStatus.NO_PLAN -> Pair("No Plan", MaterialTheme.colorScheme.outline)
                }
                StatusPill(text = statusText, color = statusColor)
            }
        }

        // Permission Warning if not yet granted
        if (!hasUsagePermission) {
            item {
                PermissionWarningBanner(onGrantClick = onGrantPermissionClick)
            }
        }

        // STATE A: No Plan Configured
        if (dataPlan == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_plan_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Let's set up your data plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Add your plan size and expiry date to track your Data Runway and daily budgets accurately.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Button(
                            onClick = onAddPlanClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_data_plan_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Data Plan")
                        }
                    }
                }
            }
        } else {
            // HERO CARD: Mobile Data Remaining
            item {
                HeroDataCard(
                    plan = dataPlan,
                    runwayInfo = runwayInfo
                )
            }
        }

        // TODAY USAGE CARD
        item {
            TodayUsageCard(
                todayBytes = todayUsageBytes,
                normalBytes = normalDailyUsageBytes,
                status = runwayInfo.status
            )
        }

        // SIGNATURE PRIMARY ACTION: 🔎 WHAT ATE MY DATA?
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
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Instant detective report on top culprits",
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

        // DATA RUNWAY CARD
        if (dataPlan != null) {
            item {
                RunwayCard(
                    runwayInfo = runwayInfo,
                    onOptimizeClick = onOptimizeClick
                )
            }
        }

        // WARNING / ANOMALY INSIGHT (if any app had unusual background or spike)
        val anomaly = anomalies.firstOrNull()
        if (anomaly != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anomaly_alert_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorCritical.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ColorCritical
                            )
                            Text(
                                text = if (anomaly.isBackgroundDominant) "Background Data Alert" else "Unusual Usage Spike",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ColorCritical
                            )
                        }
                        Text(
                            text = if (anomaly.isBackgroundDominant) {
                                "${anomaly.appName} used ${DataFormatUtils.formatBytes(anomaly.backgroundBytes)} in the background today."
                            } else {
                                "${anomaly.appName} used ${String.format("%.1f", anomaly.multipleOfNormal)}× your normal daily amount."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onWhatAteMyDataClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorCritical
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Investigate")
                            }
                        }
                    }
                }
            }
        }

        // DATA HEALTH SCORE PREVIEW
        item {
            DataHealthScoreCard(healthScore = healthScore)
        }

        // WHAT'S USING YOUR DATA? (Top 3 Apps)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "What's Using Your Data?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "See all apps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onSeeAllAppsClick)
                        .testTag("see_all_apps_button")
                )
            }
        }

        items(topApps.take(3)) { app ->
            AppUsageRow(
                app = app,
                onClick = { onAppClick(app) },
                showWifi = false
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroDataCard(
    plan: DataPlan,
    runwayInfo: DataRunwayInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_data_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "YOUR MOBILE DATA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DataFormatUtils.formatBytes(runwayInfo.remainingBytes),
                    style = MaterialTheme.typography.displaySmall,
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

            // Grandma explanation
            Text(
                text = DataFormatUtils.getGrandmaExplanation(runwayInfo.remainingBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress Bar
            val total = runwayInfo.planSizeBytes.coerceAtLeast(1L)
            val progress = (runwayInfo.usedBytesInPeriod.toFloat() / total.toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${DataFormatUtils.formatBytes(runwayInfo.usedBytesInPeriod)} used",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${runwayInfo.daysRemaining} days remaining",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TodayUsageCard(
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                if (status == RunwayStatus.LEARNING) {
                    Text(
                        text = "Learning baseline",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorInfo
                    )
                } else if (normalBytes > 0) {
                    val isAbove = todayBytes > normalBytes
                    val pct = DataFormatUtils.formatPercentageDiff(todayBytes, normalBytes)
                    Text(
                        text = (if (isAbove) "🔴 " else "🟢 ") + pct,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAbove) ColorCritical else ColorSafe
                    )
                }
            }

            Text(
                text = DataFormatUtils.formatBytes(todayBytes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (normalBytes > 0 && status != RunwayStatus.LEARNING) {
                Text(
                    text = "Your normal usage: ${DataFormatUtils.formatBytes(normalBytes)}/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "DATA GUARD is learning your normal daily usage pattern.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RunwayCard(
    runwayInfo: DataRunwayInfo,
    onOptimizeClick: () -> Unit
) {
    val isShortage = runwayInfo.expectedShortageDays > 0
    val cardColor = if (isShortage) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("runway_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATA RUNWAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${runwayInfo.runwayDaysEstimate.toInt()} days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isShortage) ColorCritical else ColorSafe
                )
            }

            Text(
                text = "At your current usage rate, your data may last approximately ${runwayInfo.runwayDaysEstimate.toInt()} days.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isShortage) {
                Text(
                    text = "⚠️ Your plan expires in ${runwayInfo.daysRemaining} days. You may run out approximately ${runwayInfo.expectedShortageDays} days early.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorCritical
                )

                Button(
                    onClick = onOptimizeClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("optimize_my_data_button")
                ) {
                    Text("Optimize my data")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ColorSafe,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "You're on track to comfortably reach your renewal date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorSafe
                    )
                }
            }
        }
    }
}

@Composable
private fun DataHealthScoreCard(healthScore: DataHealthScore) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_score_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "DATA HEALTH",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${healthScore.score} / 100",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (healthScore.score >= 75) ColorSafe else ColorWarning
                )
            }

            Text(
                text = "Rating: ${healthScore.rating}. ${healthScore.topImprovementRecommendation ?: "Your data usage habits are well managed."}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Sub-scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubScorePill("Efficiency", "${healthScore.usageEfficiencyScore}/25")
                SubScorePill("Background", "${healthScore.backgroundWasteScore}/25")
                SubScorePill("Discipline", "${healthScore.planDisciplineScore}/25")
            }
        }
    }
}

@Composable
private fun SubScorePill(label: String, score: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = score, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
