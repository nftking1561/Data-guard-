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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.components.AppUsageRow
import com.example.ui.components.SimpleUsageChart
import com.example.util.DataFormatUtils

enum class UsageSortOrder(val title: String) {
    MOST_USED("Most Used"),
    LEAST_USED("Least Used"),
    BACKGROUND("Background"),
    ALPHABETICAL("A-Z")
}

@Composable
fun UsageScreen(
    apps: List<AppUsageItem>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    showSystemApps: Boolean,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onAppClick: (AppUsageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isWifiSelected by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(UsageSortOrder.MOST_USED) }

    // Filter & sort apps
    val filteredApps = remember(apps, isWifiSelected, searchQuery, showSystemApps, sortOrder) {
        apps.filter { item ->
            // System app filter
            (showSystemApps || !item.isSystemApp) &&
                    // Search query
                    (searchQuery.isBlank() || item.appName.contains(searchQuery, ignoreCase = true) || item.packageName.contains(searchQuery, ignoreCase = true)) &&
                    // Has data in selected connection type
                    (if (isWifiSelected) item.wifiBytes > 0 else item.mobileBytes > 0)
        }.let { list ->
            when (sortOrder) {
                UsageSortOrder.MOST_USED -> if (isWifiSelected) list.sortedByDescending { it.wifiBytes } else list.sortedByDescending { it.mobileBytes }
                UsageSortOrder.LEAST_USED -> if (isWifiSelected) list.sortedBy { it.wifiBytes } else list.sortedBy { it.mobileBytes }
                UsageSortOrder.BACKGROUND -> list.sortedByDescending { it.backgroundBytes }
                UsageSortOrder.ALPHABETICAL -> list.sortedBy { it.appName.lowercase() }
            }
        }
    }

    val totalBytes = remember(apps, isWifiSelected) {
        if (isWifiSelected) apps.sumOf { it.wifiBytes } else apps.sumOf { it.mobileBytes }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Where Did My Data Go?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Accurate device breakdown measured by Android.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // TIME RANGE SELECTOR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    val isSelected = selectedTimeRange == range
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onTimeRangeSelected(range) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("time_range_${range.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // MOBILE VS WI-FI TOGGLE (CRITICAL: SEPARATED, NEVER COMBINED!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connection_type_toggle"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isWifiSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { isWifiSelected = false }
                            .padding(vertical = 12.dp)
                            .testTag("toggle_mobile_data"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mobile Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (!isWifiSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isWifiSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isWifiSelected) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            )
                            .clickable { isWifiSelected = true }
                            .padding(vertical = 12.dp)
                            .testTag("toggle_wifi_data"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Wi-Fi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isWifiSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isWifiSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // TOTAL SUMMARY CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isWifiSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isWifiSelected) "TOTAL WI-FI USAGE" else "TOTAL MOBILE DATA USAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = DataFormatUtils.formatBytes(totalBytes),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${filteredApps.size} apps active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // DISTRIBUTION CHART
        if (!isWifiSelected && filteredApps.isNotEmpty()) {
            item {
                SimpleUsageChart(items = filteredApps)
            }
        }

        // SEARCH BAR & CONTROLS
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps (e.g. Instagram)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_input")
            )
        }

        // SORT & SYSTEM APPS TOGGLE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort cycling button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            sortOrder = when (sortOrder) {
                                UsageSortOrder.MOST_USED -> UsageSortOrder.BACKGROUND
                                UsageSortOrder.BACKGROUND -> UsageSortOrder.ALPHABETICAL
                                UsageSortOrder.ALPHABETICAL -> UsageSortOrder.LEAST_USED
                                UsageSortOrder.LEAST_USED -> UsageSortOrder.MOST_USED
                            }
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sort: ${sortOrder.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Show/hide system apps
                FilterChip(
                    selected = showSystemApps,
                    onClick = { onToggleShowSystemApps(!showSystemApps) },
                    label = { Text("Show system apps", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("toggle_system_apps_chip")
                )
            }
        }

        // EMPTY STATE
        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No apps found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No application matches \"$searchQuery\"."
                            else "No apps consumed data in this period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            // LIST OF APPS
            items(filteredApps, key = { it.packageName }) { app ->
                AppUsageRow(
                    app = app,
                    onClick = { onAppClick(app) },
                    showWifi = isWifiSelected
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
