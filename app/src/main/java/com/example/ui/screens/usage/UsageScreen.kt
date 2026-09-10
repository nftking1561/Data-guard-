package com.example.ui.screens.usage

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUsageItem
import com.example.data.model.TimeRange
import com.example.ui.theme.ColorCritical
import com.example.ui.theme.ColorSafe
import com.example.ui.theme.ColorWarning
import com.example.util.DataFormatUtils

enum class UsageViewTab {
    TOP_APPS,
    BACKGROUND
}

@Composable
fun UsageScreen(
    apps: List<AppUsageItem>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    showSystemApps: Boolean,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onAppClick: (AppUsageItem) -> Unit,
    onInvestigateClick: ((AppUsageItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isWifiSelected by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(UsageViewTab.TOP_APPS) }

    val totalMobile = remember(apps) { apps.sumOf { it.mobileBytes } }
    val totalWifi = remember(apps) { apps.sumOf { it.wifiBytes } }

    val displayApps = remember(apps, isWifiSelected, currentTab, showSystemApps) {
        val base = apps.filter { showSystemApps || !it.isSystemApp }
        when (currentTab) {
            UsageViewTab.TOP_APPS -> {
                if (isWifiSelected) {
                    base.filter { it.wifiBytes > 0 }.sortedByDescending { it.wifiBytes }
                } else {
                    base.filter { it.mobileBytes > 0 }.sortedByDescending { it.mobileBytes }
                }
            }
            UsageViewTab.BACKGROUND -> {
                base.filter { it.backgroundBytes > 0 }.sortedByDescending { it.backgroundBytes }
            }
        }
    }

    // Identify unusual app if any (e.g. background dominance or high usage)
    val unusualApp = remember(apps) {
        apps.firstOrNull { it.mobileBytes > 200L * 1024 * 1024 && (it.hasBackgroundDominance || it.mobileBytes > 400L * 1024 * 1024) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP TITLE
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WHERE?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                // Time selector: Today | 7D | 30D
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TimeRange.entries.forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onTimeRangeSelected(range) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("time_range_${range.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // MOBILE VS WI-FI (DISTINCTION OBVIOUS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mobile Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isWifiSelected = false }
                        .testTag("toggle_mobile_data"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isWifiSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (!isWifiSelected) 2.dp else 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "MOBILE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!isWifiSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = DataFormatUtils.formatBytes(totalMobile),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Wi-Fi Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isWifiSelected = true }
                        .testTag("toggle_wifi_data"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWifiSelected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isWifiSelected) 2.dp else 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "WI-FI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isWifiSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = DataFormatUtils.formatBytes(totalWifi),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // UNUSUAL USAGE BANNER (Section 21)
        if (unusualApp != null && !isWifiSelected) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorCritical.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ UNUSUAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorCritical,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "4.5× normal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorCritical
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = unusualApp.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${DataFormatUtils.formatBytes(unusualApp.mobileBytes)} • Possible cause: Video / media",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onInvestigateClick?.invoke(unusualApp) ?: onAppClick(unusualApp) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorCritical
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("INVESTIGATE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB SELECTOR: TOP APPS | BACKGROUND
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tabs = listOf(
                        UsageViewTab.TOP_APPS to "Top Apps",
                        UsageViewTab.BACKGROUND to "Background"
                    )
                    tabs.forEach { (tab, title) ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else Color.Transparent
                                )
                                .clickable { currentTab = tab }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // System Apps Toggle
                Text(
                    text = if (showSystemApps) "Hide system" else "Show system",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onToggleShowSystemApps(!showSystemApps) }
                )
            }
        }

        // RANKED APP LIST (1, 2, 3... with app icon, app name, bold byte number)
        itemsIndexed(displayApps) { index, app ->
            val displayBytes = when {
                currentTab == UsageViewTab.BACKGROUND -> app.backgroundBytes
                isWifiSelected -> app.wifiBytes
                else -> app.mobileBytes
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(app) }
                    .testTag("app_rank_item_${app.packageName}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.width(20.dp)
                        )

                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = DataFormatUtils.formatBytes(displayBytes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
