package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppUsageItem
import com.example.ui.components.NavigationDestination
import com.example.ui.dialogs.AppDetailDialog
import com.example.ui.dialogs.CarrierBalanceDialog
import com.example.ui.dialogs.EditPlanDialog
import com.example.ui.screens.control.ControlScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.investigate.InvestigateScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.usage.UsageScreen
import com.example.util.PermissionUtils

@Composable
fun DataGuardApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle to auto-refresh onResume (e.g. after granting permission)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission()
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val currentPlan by viewModel.currentPlan.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val runwayInfo by viewModel.runwayInfo.collectAsState()
    val todayUsageBytes by viewModel.todayUsageBytes.collectAsState()
    val normalDailyUsageBytes by viewModel.normalDailyUsageBytes.collectAsState()
    val appsUsageList by viewModel.appsUsageList.collectAsState()
    val anomalies by viewModel.anomalies.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val showSystemApps by viewModel.showSystemApps.collectAsState()
    val investigationResult by viewModel.investigationResult.collectAsState()
    val isInvestigating by viewModel.isInvestigating.collectAsState()
    val selectedInvestigationTopic by viewModel.selectedInvestigationTopic.collectAsState()
    val dataSaverMode by viewModel.dataSaverMode.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val carrierName by viewModel.carrierName.collectAsState()

    var currentTab by remember { mutableStateOf(NavigationDestination.HOME) }
    var selectedAppForDetail by remember { mutableStateOf<AppUsageItem?>(null) }
    var showEditPlanDialog by remember { mutableStateOf(false) }
    var showCarrierReconciliationDialog by remember { mutableStateOf(false) }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onComplete = { carrier, plan ->
                viewModel.completeOnboarding(carrier, plan)
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    NavigationDestination.entries.forEach { destination ->
                        val isSelected = currentTab == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = destination
                                if (destination == NavigationDestination.INVESTIGATE && investigationResult == null) {
                                    viewModel.runInvestigation()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_item_${destination.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavigationDestination.HOME -> {
                        HomeScreen(
                            dataPlan = currentPlan,
                            runwayInfo = runwayInfo,
                            todayUsageBytes = todayUsageBytes,
                            normalDailyUsageBytes = normalDailyUsageBytes,
                            topApps = appsUsageList,
                            anomalies = anomalies,
                            healthScore = healthScore,
                            hasUsagePermission = hasUsagePermission,
                            onGrantPermissionClick = {
                                PermissionUtils.openUsageAccessSettings(context)
                            },
                            onAppClick = { app ->
                                selectedAppForDetail = app
                            },
                            onSeeAllAppsClick = {
                                currentTab = NavigationDestination.USAGE
                            },
                            onAddPlanClick = {
                                showEditPlanDialog = true
                            },
                            onWhatAteMyDataClick = {
                                currentTab = NavigationDestination.INVESTIGATE
                                viewModel.runInvestigation("My data disappeared quickly")
                            },
                            onOptimizeClick = {
                                currentTab = NavigationDestination.CONTROL
                            }
                        )
                    }
                    NavigationDestination.USAGE -> {
                        UsageScreen(
                            apps = appsUsageList,
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = { viewModel.setTimeRange(it) },
                            showSystemApps = showSystemApps,
                            onToggleShowSystemApps = { viewModel.setShowSystemApps(it) },
                            onAppClick = { app ->
                                selectedAppForDetail = app
                            }
                        )
                    }
                    NavigationDestination.INVESTIGATE -> {
                        InvestigateScreen(
                            result = investigationResult,
                            isLoading = isInvestigating,
                            selectedTopic = selectedInvestigationTopic,
                            onSelectTopic = { topic ->
                                viewModel.runInvestigation(topic)
                            },
                            onAppClick = { app ->
                                selectedAppForDetail = app
                            },
                            onTakeMeToSettingClick = { pkg ->
                                if (pkg != null) {
                                    PermissionUtils.openAppDetailsSettings(context, pkg)
                                } else {
                                    PermissionUtils.openDataSaverSettings(context)
                                }
                            }
                        )
                    }
                    NavigationDestination.CONTROL -> {
                        ControlScreen(
                            currentDataSaverMode = dataSaverMode,
                            onSetDataSaverMode = { viewModel.setDataSaverMode(it) },
                            backgroundHeavyApps = appsUsageList.filter { it.hasBackgroundDominance },
                            onOpenAndroidDataSaver = {
                                PermissionUtils.openDataSaverSettings(context)
                            },
                            onOpenAppSettings = { pkg ->
                                PermissionUtils.openAppDetailsSettings(context, pkg)
                            },
                            onAppClick = { app ->
                                selectedAppForDetail = app
                            }
                        )
                    }
                    NavigationDestination.SETTINGS -> {
                        SettingsScreen(
                            currentPlan = currentPlan,
                            onEditPlanClick = {
                                showEditPlanDialog = true
                            },
                            onCarrierReconcileClick = {
                                showCarrierReconciliationDialog = true
                            },
                            alertsEnabled = alertsEnabled,
                            onToggleAlerts = { viewModel.toggleAlerts(it) },
                            hasUsagePermission = hasUsagePermission,
                            onGrantPermissionClick = {
                                PermissionUtils.openUsageAccessSettings(context)
                            },
                            carrierName = carrierName
                        )
                    }
                }
            }
        }

        // Active Dialogs
        selectedAppForDetail?.let { app ->
            AppDetailDialog(
                app = app,
                onDismiss = { selectedAppForDetail = null },
                onOpenAndroidSettings = { pkg ->
                    PermissionUtils.openAppDetailsSettings(context, pkg)
                }
            )
        }

        if (showEditPlanDialog) {
            EditPlanDialog(
                currentPlan = currentPlan,
                onSave = { plan ->
                    viewModel.savePlan(plan)
                    showEditPlanDialog = false
                },
                onDismiss = { showEditPlanDialog = false }
            )
        }

        if (showCarrierReconciliationDialog) {
            CarrierBalanceDialog(
                deviceRemainingBytes = runwayInfo.remainingBytes,
                onSaveReconciliation = { carrierBytes, notes ->
                    viewModel.saveCarrierReconciliation(carrierBytes, notes)
                },
                onDismiss = { showCarrierReconciliationDialog = false }
            )
        }
    }
}
