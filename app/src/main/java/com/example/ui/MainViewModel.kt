package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calculator.AnomalyDetectionCalculator
import com.example.data.calculator.DataHealthScoreCalculator
import com.example.data.calculator.DataRunwayCalculator
import com.example.data.local.AppDatabase
import com.example.data.model.AnomalyInfo
import com.example.data.model.AppUsageItem
import com.example.data.model.DataHealthScore
import com.example.data.model.DataPlan
import com.example.data.model.DataRunwayInfo
import com.example.data.model.InvestigationResult
import com.example.data.model.RunwayStatus
import com.example.data.model.TimeRange
import com.example.data.repository.DataPlanRepository
import com.example.data.repository.InvestigationRepository
import com.example.data.repository.NetworkStatsRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.util.NotificationHelper
import com.example.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dataPlanRepository = DataPlanRepository(db.dataPlanDao(), db.carrierDao(), db.appAlertDao())
    private val networkStatsRepository = NetworkStatsRepository(application, db.usageSnapshotDao())
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val investigationRepository = InvestigationRepository(networkStatsRepository)

    val isOnboardingCompleted: StateFlow<Boolean> = userPreferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentPlan: StateFlow<DataPlan?> = dataPlanRepository.activePlanFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _hasUsagePermission = MutableStateFlow(PermissionUtils.hasUsageStatsPermission(application))
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.TODAY)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _showSystemApps = MutableStateFlow(false)
    val showSystemApps: StateFlow<Boolean> = _showSystemApps.asStateFlow()

    private val _appsUsageList = MutableStateFlow<List<AppUsageItem>>(emptyList())
    val appsUsageList: StateFlow<List<AppUsageItem>> = _appsUsageList.asStateFlow()

    private val _todayUsageBytes = MutableStateFlow(0L)
    val todayUsageBytes: StateFlow<Long> = _todayUsageBytes.asStateFlow()

    private val _normalDailyUsageBytes = MutableStateFlow(0L)
    val normalDailyUsageBytes: StateFlow<Long> = _normalDailyUsageBytes.asStateFlow()

    private val _runwayInfo = MutableStateFlow(
        DataRunwayInfo(
            hasPlan = false,
            planSizeBytes = 0L,
            usedBytesInPeriod = 0L,
            remainingBytes = 0L,
            daysRemaining = 0,
            totalPlanDays = 0,
            daysElapsed = 0,
            sustainableDailyBytes = 0L,
            recentDailyAverageBytes = 0L,
            runwayDaysEstimate = 0.0,
            projectedExhaustionDateEpochMs = null,
            expectedShortageDays = 0,
            status = RunwayStatus.NO_PLAN,
            dailyTargetBytes = 0L,
            todayFlexibilityBytes = 0L
        )
    )
    val runwayInfo: StateFlow<DataRunwayInfo> = _runwayInfo.asStateFlow()

    private val _anomalies = MutableStateFlow<List<AnomalyInfo>>(emptyList())
    val anomalies: StateFlow<List<AnomalyInfo>> = _anomalies.asStateFlow()

    private val _healthScore = MutableStateFlow(
        DataHealthScore(
            score = 85,
            rating = "Good",
            planDisciplineScore = 20,
            usageEfficiencyScore = 22,
            backgroundWasteScore = 21,
            unexpectedActivityScore = 22,
            topImprovementRecommendation = "Monitor background-heavy applications.",
            actionPackageName = null
        )
    )
    val healthScore: StateFlow<DataHealthScore> = _healthScore.asStateFlow()

    private val _investigationResult = MutableStateFlow<InvestigationResult?>(null)
    val investigationResult: StateFlow<InvestigationResult?> = _investigationResult.asStateFlow()

    private val _isInvestigating = MutableStateFlow(false)
    val isInvestigating: StateFlow<Boolean> = _isInvestigating.asStateFlow()

    private val _selectedInvestigationTopic = MutableStateFlow("My data disappeared quickly")
    val selectedInvestigationTopic: StateFlow<String> = _selectedInvestigationTopic.asStateFlow()

    val dataSaverMode: StateFlow<String> = userPreferencesRepository.dataSaverMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "NORMAL")

    val alertsEnabled: StateFlow<Boolean> = userPreferencesRepository.runwayAlertEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _carrierName = MutableStateFlow("MTN")
    val carrierName: StateFlow<String> = _carrierName.asStateFlow()

    init {
        NotificationHelper.initNotificationChannel(application)
        loadData()
    }

    fun checkPermission() {
        val granted = PermissionUtils.hasUsageStatsPermission(getApplication())
        _hasUsagePermission.value = granted
        if (granted) {
            refreshData()
        }
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadAppsForRange(timeRange)
    }

    fun setShowSystemApps(show: Boolean) {
        _showSystemApps.value = show
    }

    fun refreshData() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            checkPermission()
            val plan = currentPlan.value ?: dataPlanRepository.getActivePlan()

            // Load usage for today
            val todayApps = networkStatsRepository.getUsageData(TimeRange.TODAY)
            val todayBytes = todayApps.sumOf { it.mobileBytes }
            _todayUsageBytes.value = todayBytes

            // Load usage for current selected range
            loadAppsForRange(_selectedTimeRange.value)

            // Calculate historical baseline
            val historyCount = networkStatsRepository.getHistoricalDaysCount()
            val baselineMap = networkStatsRepository.getBaselineAverages()
            val normal = if (baselineMap.isNotEmpty()) {
                baselineMap.values.sum()
            } else {
                350L * 1024 * 1024 // 350 MB normal daily baseline
            }
            _normalDailyUsageBytes.value = normal

            // Calculate runway
            val runway = DataRunwayCalculator.calculateRunway(
                plan = plan,
                measuredUsageInPlanPeriodBytes = todayBytes,
                recentDailyAverageBytes = normal,
                hasSufficientHistory = historyCount >= 3
            )
            _runwayInfo.value = runway

            // Calculate anomalies
            val detectedAnomalies = AnomalyDetectionCalculator.detectAnomalies(
                historicalDaysCount = historyCount,
                todayApps = todayApps,
                baselineAveragesByPackage = baselineMap
            )
            _anomalies.value = detectedAnomalies

            // Calculate health score
            val health = DataHealthScoreCalculator.calculateScore(
                runwayInfo = runway,
                todayApps = todayApps,
                anomalies = detectedAnomalies
            )
            _healthScore.value = health
        }
    }

    private fun loadAppsForRange(range: TimeRange) {
        viewModelScope.launch {
            val apps = networkStatsRepository.getUsageData(range)
            _appsUsageList.value = apps
        }
    }

    fun completeOnboarding(carrier: String, plan: DataPlan?) {
        viewModelScope.launch {
            _carrierName.value = carrier
            userPreferencesRepository.setSelectedCarrier(carrier)
            if (plan != null) {
                dataPlanRepository.savePlan(plan)
            }
            userPreferencesRepository.setOnboardingCompleted(true)
            loadData()
        }
    }

    fun savePlan(plan: DataPlan) {
        viewModelScope.launch {
            dataPlanRepository.savePlan(plan)
            loadData()
        }
    }

    fun runInvestigation(topic: String = _selectedInvestigationTopic.value) {
        _selectedInvestigationTopic.value = topic
        _isInvestigating.value = true
        viewModelScope.launch {
            val result = investigationRepository.runInvestigation(topic)
            _investigationResult.value = result
            _isInvestigating.value = false
        }
    }

    fun setDataSaverMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDataSaverMode(mode)
        }
    }

    fun toggleAlerts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRunwayAlertEnabled(enabled)
            userPreferencesRepository.setAbnormalAlertEnabled(enabled)
        }
    }

    fun saveCarrierReconciliation(carrierBytes: Long, notes: String) {
        viewModelScope.launch {
            val deviceRemaining = _runwayInfo.value.remainingBytes
            dataPlanRepository.recordCarrierBalance(
                carrierBalanceBytes = carrierBytes,
                deviceMeasuredBytes = deviceRemaining,
                notes = notes
            )
        }
    }
}
