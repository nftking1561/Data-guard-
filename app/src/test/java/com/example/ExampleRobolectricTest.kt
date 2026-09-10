package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.calculator.AnomalyDetectionCalculator
import com.example.data.calculator.DataHealthScoreCalculator
import com.example.data.calculator.DataRunwayCalculator
import com.example.data.model.AppCategory
import com.example.data.model.AppUsageItem
import com.example.data.model.BudgetMode
import com.example.data.model.DataPlan
import com.example.data.model.RunwayStatus
import com.example.util.DataFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Data Guard", appName)
    }

    @Test
    fun testDataFormatUtilsFormatting() {
        val bytes500MB = 500L * 1024 * 1024
        val formatted = DataFormatUtils.formatBytes(bytes500MB, useBinary = true)
        assertEquals("500 MB", formatted)

        val bytes13GB = (13.16 * 1024 * 1024 * 1024).toLong()
        val formattedGb = DataFormatUtils.formatBytes(bytes13GB, useBinary = true)
        assertEquals("13.16 GB", formattedGb)

        val grandma = DataFormatUtils.getGrandmaExplanation(bytes13GB)
        assertTrue(grandma.contains("Solid amount"))

        val bytes2GB = (2.5 * 1024 * 1024 * 1024).toLong()
        val grandma2 = DataFormatUtils.getGrandmaExplanation(bytes2GB)
        assertTrue(grandma2.contains("gigabytes"))
    }

    @Test
    fun testRunwayCalculationOnTrack() {
        val now = System.currentTimeMillis()
        val plan = DataPlan(
            planSizeBytes = 20L * 1024 * 1024 * 1024,
            startDateEpochMs = now - (10L * 24 * 60 * 60 * 1000),
            expiryDateEpochMs = now + (20L * 24 * 60 * 60 * 1000),
            budgetMode = BudgetMode.SMART
        )

        val runway = DataRunwayCalculator.calculateRunway(
            plan = plan,
            measuredUsageInPlanPeriodBytes = 5L * 1024 * 1024 * 1024,
            recentDailyAverageBytes = 500L * 1024 * 1024, // 500MB per day
            hasSufficientHistory = true
        )

        assertTrue(runway.hasPlan)
        assertEquals(RunwayStatus.ON_TRACK, runway.status)
        assertEquals(0, runway.expectedShortageDays)
        assertTrue(runway.runwayDaysEstimate >= 20.0)
    }

    @Test
    fun testRunwayCalculationShortage() {
        val now = System.currentTimeMillis()
        val plan = DataPlan(
            planSizeBytes = 10L * 1024 * 1024 * 1024,
            startDateEpochMs = now - (10L * 24 * 60 * 60 * 1000),
            expiryDateEpochMs = now + (20L * 24 * 60 * 60 * 1000),
            budgetMode = BudgetMode.SMART
        )

        // Only 2 GB remaining with 20 days left, but burning 1 GB per day
        val runway = DataRunwayCalculator.calculateRunway(
            plan = plan,
            measuredUsageInPlanPeriodBytes = 8L * 1024 * 1024 * 1024,
            recentDailyAverageBytes = 1L * 1024 * 1024 * 1024,
            hasSufficientHistory = true
        )

        assertEquals(RunwayStatus.CRITICAL_SHORTAGE, runway.status)
        assertTrue(runway.expectedShortageDays > 0)
    }

    @Test
    fun testAnomalyDetectionNewUserRule() {
        // Under 3 days of historical data: must never trigger false anomalies
        val todayApps = listOf(
            AppUsageItem(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                uid = 10101,
                category = AppCategory.VIDEO,
                mobileBytes = 800L * 1024 * 1024,
                wifiBytes = 0L,
                foregroundBytes = 750L * 1024 * 1024,
                backgroundBytes = 50L * 1024 * 1024
            )
        )

        val anomalies = AnomalyDetectionCalculator.detectAnomalies(
            historicalDaysCount = 1, // Only 1 day history
            todayApps = todayApps,
            baselineAveragesByPackage = emptyMap()
        )

        assertTrue(anomalies.isEmpty())
    }

    @Test
    fun testAnomalyDetectionSpikeAndBackground() {
        val todayApps = listOf(
            AppUsageItem(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                uid = 10101,
                category = AppCategory.VIDEO,
                mobileBytes = 500L * 1024 * 1024,
                wifiBytes = 0L,
                foregroundBytes = 450L * 1024 * 1024,
                backgroundBytes = 50L * 1024 * 1024
            ),
            AppUsageItem(
                packageName = "com.google.android.apps.photos",
                appName = "Google Photos",
                uid = 10102,
                category = AppCategory.CLOUD,
                mobileBytes = 80L * 1024 * 1024,
                wifiBytes = 0L,
                foregroundBytes = 10L * 1024 * 1024,
                backgroundBytes = 70L * 1024 * 1024 // High background dominance
            )
        )

        val baselines = mapOf(
            "com.google.android.youtube" to 150L * 1024 * 1024 // Normal was 150MB, today is 500MB (3.3x surge)
        )

        val anomalies = AnomalyDetectionCalculator.detectAnomalies(
            historicalDaysCount = 7, // 7 days of history
            todayApps = todayApps,
            baselineAveragesByPackage = baselines
        )

        assertFalse(anomalies.isEmpty())
        val youtubeAnomaly = anomalies.find { it.packageName == "com.google.android.youtube" }
        assertTrue(youtubeAnomaly != null && youtubeAnomaly.multipleOfNormal > 2.0)

        val photosAnomaly = anomalies.find { it.packageName == "com.google.android.apps.photos" }
        assertTrue(photosAnomaly != null && photosAnomaly.isBackgroundDominant)
    }

    @Test
    fun testDataHealthScoreDeterministic() {
        val now = System.currentTimeMillis()
        val plan = DataPlan(
            planSizeBytes = 20L * 1024 * 1024 * 1024,
            startDateEpochMs = now - (10L * 24 * 60 * 60 * 1000),
            expiryDateEpochMs = now + (20L * 24 * 60 * 60 * 1000),
            budgetMode = BudgetMode.SMART
        )

        val runway = DataRunwayCalculator.calculateRunway(
            plan = plan,
            measuredUsageInPlanPeriodBytes = 4L * 1024 * 1024 * 1024,
            recentDailyAverageBytes = 400L * 1024 * 1024,
            hasSufficientHistory = true
        )

        val health = DataHealthScoreCalculator.calculateScore(
            runwayInfo = runway,
            todayApps = emptyList(),
            anomalies = emptyList()
        )

        assertTrue(health.score in 0..100)
        assertTrue(health.score >= 80)
        assertEquals("Excellent", health.rating)
    }
}
