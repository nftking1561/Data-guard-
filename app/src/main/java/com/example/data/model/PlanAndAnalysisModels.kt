package com.example.data.model

data class DataPlan(
    val id: Long = 1L,
    val networkCarrier: String = "MTN",
    val planName: String = "Standard Monthly Plan",
    val planSizeBytes: Long = 20L * 1024 * 1024 * 1024, // default 20 GB
    val priceNgn: Double = 5000.0,
    val startDateEpochMs: Long = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000), // 14 days ago
    val expiryDateEpochMs: Long = System.currentTimeMillis() + (16L * 24 * 60 * 60 * 1000), // 16 days remaining
    val autoRenewal: Boolean = true,
    val budgetMode: BudgetMode = BudgetMode.SMART,
    val manualCarrierBalanceBytes: Long? = null,
    val manualCarrierBalanceTimestamp: Long? = null
)

data class DataRunwayInfo(
    val hasPlan: Boolean,
    val planSizeBytes: Long,
    val usedBytesInPeriod: Long,
    val remainingBytes: Long,
    val daysRemaining: Int,
    val totalPlanDays: Int,
    val daysElapsed: Int,
    val sustainableDailyBytes: Long,
    val recentDailyAverageBytes: Long,
    val runwayDaysEstimate: Double,
    val projectedExhaustionDateEpochMs: Long?,
    val expectedShortageDays: Int,
    val status: RunwayStatus,
    val dailyTargetBytes: Long,
    val todayFlexibilityBytes: Long
)

enum class RunwayStatus(val label: String) {
    ON_TRACK("On Track"),
    FASTER_THAN_USUAL("Using Faster Than Usual"),
    CRITICAL_SHORTAGE("May Run Out Early"),
    LEARNING("Learning Normal Usage"),
    NO_PLAN("No Plan Configured")
}

data class AnomalyInfo(
    val packageName: String,
    val appName: String,
    val observedBytes: Long,
    val baselineBytes: Long,
    val multipleOfNormal: Double,
    val possibleReasons: List<String>,
    val recommendation: String,
    val isBackgroundDominant: Boolean,
    val backgroundBytes: Long
)

data class DataHealthScore(
    val score: Int, // 0 - 100
    val rating: String, // Excellent, Good, Needs Attention, High Risk
    val usageEfficiencyScore: Int, // max 25
    val backgroundWasteScore: Int, // max 25
    val unexpectedActivityScore: Int, // max 25
    val planDisciplineScore: Int, // max 25
    val topImprovementRecommendation: String?,
    val actionPackageName: String?
)

data class InvestigationResult(
    val title: String,
    val summary: String,
    val totalBytesUsed: Long,
    val topApps: List<AppUsageItem>,
    val biggestContributor: AppUsageItem?,
    val backgroundCulprit: AppUsageItem?,
    val explanations: List<String>,
    val recommendedAction: String,
    val targetPackageName: String?
)
