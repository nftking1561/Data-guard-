package com.example.data.calculator

import com.example.data.model.BudgetMode
import com.example.data.model.DataPlan
import com.example.data.model.DataRunwayInfo
import com.example.data.model.RunwayStatus
import kotlin.math.max

object DataRunwayCalculator {

    fun calculateRunway(
        plan: DataPlan?,
        measuredUsageInPlanPeriodBytes: Long,
        recentDailyAverageBytes: Long,
        hasSufficientHistory: Boolean = true
    ): DataRunwayInfo {
        if (plan == null) {
            return DataRunwayInfo(
                hasPlan = false,
                planSizeBytes = 0L,
                usedBytesInPeriod = measuredUsageInPlanPeriodBytes,
                remainingBytes = 0L,
                daysRemaining = 0,
                totalPlanDays = 0,
                daysElapsed = 0,
                sustainableDailyBytes = 0L,
                recentDailyAverageBytes = recentDailyAverageBytes,
                runwayDaysEstimate = 0.0,
                projectedExhaustionDateEpochMs = null,
                expectedShortageDays = 0,
                status = RunwayStatus.NO_PLAN,
                dailyTargetBytes = 0L,
                todayFlexibilityBytes = 0L
            )
        }

        val now = System.currentTimeMillis()
        val totalMs = max(1L, plan.expiryDateEpochMs - plan.startDateEpochMs)
        val totalDays = max(1, (totalMs / (24L * 60 * 60 * 1000)).toInt())

        val elapsedMs = max(0L, now - plan.startDateEpochMs)
        val daysElapsed = (elapsedMs / (24L * 60 * 60 * 1000)).toInt()

        val remainingMs = max(0L, plan.expiryDateEpochMs - now)
        val daysRemaining = max(1, ((remainingMs + (12L * 60 * 60 * 1000)) / (24L * 60 * 60 * 1000)).toInt())

        // Use manual carrier balance if set and more recent than plan start, otherwise plan - measured
        val remainingBytes = if (plan.manualCarrierBalanceBytes != null) {
            max(0L, plan.manualCarrierBalanceBytes)
        } else {
            max(0L, plan.planSizeBytes - measuredUsageInPlanPeriodBytes)
        }

        val sustainableDailyBytes = remainingBytes / daysRemaining

        // Daily target according to budget mode:
        val dailyTargetBytes = when (plan.budgetMode) {
            BudgetMode.FIXED -> plan.planSizeBytes / totalDays
            BudgetMode.SMART -> sustainableDailyBytes
        }

        // Today flexibility in SMART mode:
        val todayFlexibilityBytes = max(0L, sustainableDailyBytes - (recentDailyAverageBytes / 2))

        val avgDailyUsage = if (recentDailyAverageBytes > 0) recentDailyAverageBytes else (dailyTargetBytes / 2)

        val runwayDaysEstimate = if (avgDailyUsage > 0) {
            remainingBytes.toDouble() / avgDailyUsage.toDouble()
        } else {
            daysRemaining.toDouble()
        }

        val projectedExhaustionMs = if (avgDailyUsage > 0) {
            now + (runwayDaysEstimate * 24L * 60 * 60 * 1000).toLong()
        } else {
            plan.expiryDateEpochMs
        }

        val expectedShortageDays = if (runwayDaysEstimate < daysRemaining) {
            (daysRemaining - runwayDaysEstimate).toInt().coerceAtLeast(1)
        } else {
            0
        }

        val status = when {
            !hasSufficientHistory -> RunwayStatus.LEARNING
            expectedShortageDays >= 3 -> RunwayStatus.CRITICAL_SHORTAGE
            runwayDaysEstimate < daysRemaining -> RunwayStatus.FASTER_THAN_USUAL
            else -> RunwayStatus.ON_TRACK
        }

        return DataRunwayInfo(
            hasPlan = true,
            planSizeBytes = plan.planSizeBytes,
            usedBytesInPeriod = measuredUsageInPlanPeriodBytes,
            remainingBytes = remainingBytes,
            daysRemaining = daysRemaining,
            totalPlanDays = totalDays,
            daysElapsed = daysElapsed,
            sustainableDailyBytes = sustainableDailyBytes,
            recentDailyAverageBytes = avgDailyUsage,
            runwayDaysEstimate = runwayDaysEstimate,
            projectedExhaustionDateEpochMs = projectedExhaustionMs,
            expectedShortageDays = expectedShortageDays,
            status = status,
            dailyTargetBytes = dailyTargetBytes,
            todayFlexibilityBytes = todayFlexibilityBytes
        )
    }
}
