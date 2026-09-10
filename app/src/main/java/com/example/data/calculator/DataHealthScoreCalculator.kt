package com.example.data.calculator

import com.example.data.model.AnomalyInfo
import com.example.data.model.AppUsageItem
import com.example.data.model.DataHealthScore
import com.example.data.model.DataRunwayInfo
import com.example.data.model.RunwayStatus

object DataHealthScoreCalculator {

    /**
     * Deterministic scoring model based on 4 pillars (max 25 each):
     * 1. Usage Efficiency: Are you pacing within sustainable daily target?
     * 2. Background Waste Control: Are apps silently guzzling cellular data?
     * 3. Unexpected Activity: Any unexplained spikes or anomalies?
     * 4. Plan Discipline: Configured plan with healthy runway buffer?
     */
    fun calculateScore(
        runwayInfo: DataRunwayInfo,
        todayApps: List<AppUsageItem>,
        anomalies: List<AnomalyInfo>
    ): DataHealthScore {
        // Pillar 1: Usage Efficiency (0-25)
        val efficiencyScore = when (runwayInfo.status) {
            RunwayStatus.ON_TRACK -> 25
            RunwayStatus.LEARNING -> 22
            RunwayStatus.FASTER_THAN_USUAL -> 15
            RunwayStatus.CRITICAL_SHORTAGE -> 8
            RunwayStatus.NO_PLAN -> 18
        }

        // Pillar 2: Background Waste (0-25)
        // Find background-heavy apps
        val highBackgroundApp = todayApps.firstOrNull { it.hasBackgroundDominance && it.backgroundBytes > 25 * 1024 * 1024L }
        val backgroundScore = when {
            highBackgroundApp == null -> 25
            highBackgroundApp.backgroundBytes > 100 * 1024 * 1024L -> 10
            highBackgroundApp.backgroundBytes > 50 * 1024 * 1024L -> 16
            else -> 20
        }

        // Pillar 3: Unexpected Activity (0-25)
        val unexpectedScore = when {
            anomalies.isEmpty() -> 25
            anomalies.size == 1 -> 18
            anomalies.size == 2 -> 12
            else -> 8
        }

        // Pillar 4: Plan Discipline & Runway (0-25)
        val planDisciplineScore = if (!runwayInfo.hasPlan) {
            15 // Neutral if no plan yet
        } else if (runwayInfo.expectedShortageDays == 0) {
            25
        } else if (runwayInfo.expectedShortageDays <= 2) {
            18
        } else {
            10
        }

        val totalScore = (efficiencyScore + backgroundScore + unexpectedScore + planDisciplineScore).coerceIn(0, 100)

        val rating = when {
            totalScore >= 90 -> "Excellent"
            totalScore >= 75 -> "Good"
            totalScore >= 50 -> "Needs attention"
            else -> "High data risk"
        }

        // Top recommendation
        var topRecommendation: String? = null
        var actionPackageName: String? = null

        if (highBackgroundApp != null) {
            topRecommendation = "${highBackgroundApp.appName} used significant mobile data in the background."
            actionPackageName = highBackgroundApp.packageName
        } else if (anomalies.isNotEmpty()) {
            val firstAnomaly = anomalies.first()
            topRecommendation = "${firstAnomaly.appName} is consuming more mobile data than usual today."
            actionPackageName = firstAnomaly.packageName
        } else if (runwayInfo.hasPlan && runwayInfo.expectedShortageDays > 0) {
            topRecommendation = "You may run out of data about ${runwayInfo.expectedShortageDays} days before renewal."
        }

        return DataHealthScore(
            score = totalScore,
            rating = rating,
            usageEfficiencyScore = efficiencyScore,
            backgroundWasteScore = backgroundScore,
            unexpectedActivityScore = unexpectedScore,
            planDisciplineScore = planDisciplineScore,
            topImprovementRecommendation = topRecommendation,
            actionPackageName = actionPackageName
        )
    }
}
