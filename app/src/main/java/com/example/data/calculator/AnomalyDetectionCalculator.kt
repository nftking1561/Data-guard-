package com.example.data.calculator

import com.example.data.model.AnomalyInfo
import com.example.data.model.AppCategory
import com.example.data.model.AppUsageItem

object AnomalyDetectionCalculator {

    /**
     * @param historicalDaysCount Number of distinct historical days recorded
     * @param todayApps Current today usage per app
     * @param baselineAveragesByPackage Average daily mobile usage mapped by packageName
     */
    fun detectAnomalies(
        historicalDaysCount: Int,
        todayApps: List<AppUsageItem>,
        baselineAveragesByPackage: Map<String, Long>
    ): List<AnomalyInfo> {
        // Enforce the New User Anomaly Rule:
        // Do not trigger anomaly warnings when there is insufficient historical data.
        if (historicalDaysCount < 3) {
            return emptyList()
        }

        val anomalies = mutableListOf<AnomalyInfo>()

        for (app in todayApps) {
            if (app.mobileBytes < 25 * 1024 * 1024L) continue // Ignore negligible usage under 25MB

            val baseline = baselineAveragesByPackage[app.packageName] ?: (app.mobileBytes / 2)
            val multiple = if (baseline > 0) app.mobileBytes.toDouble() / baseline.toDouble() else 1.0

            val isSurge = multiple >= 1.7 && (app.mobileBytes - baseline) >= 40 * 1024 * 1024L
            val isBackgroundDominant = app.hasBackgroundDominance && app.backgroundBytes > 30 * 1024 * 1024L

            if (isSurge || isBackgroundDominant) {
                val reasons = getPossibleReasons(app.category, isBackgroundDominant)
                val recommendation = when {
                    isBackgroundDominant -> "Consider restricting ${app.appName} from using mobile data in the background."
                    app.category == AppCategory.VIDEO -> "Consider lowering video streaming quality or downloading media over Wi-Fi."
                    app.category == AppCategory.CLOUD -> "Check if automatic backup / sync is set to Wi-Fi only."
                    else -> "Review in-app media auto-download and background sync preferences."
                }

                anomalies.add(
                    AnomalyInfo(
                        packageName = app.packageName,
                        appName = app.appName,
                        observedBytes = app.mobileBytes,
                        baselineBytes = baseline,
                        multipleOfNormal = multiple,
                        possibleReasons = reasons,
                        recommendation = recommendation,
                        isBackgroundDominant = isBackgroundDominant,
                        backgroundBytes = app.backgroundBytes
                    )
                )
            }
        }

        return anomalies.sortedByDescending { it.observedBytes }
    }

    private fun getPossibleReasons(category: AppCategory, isBackground: Boolean): List<String> {
        return if (isBackground) {
            listOf(
                "Automatic cloud backup / sync in the background",
                "Silent media pre-fetching or pending uploads",
                "Background notification and message synchronization"
            )
        } else {
            when (category) {
                AppCategory.VIDEO -> listOf(
                    "High-definition video streaming",
                    "Video downloads or playlist caching",
                    "Autoplay video content in feed"
                )
                AppCategory.SOCIAL -> listOf(
                    "High-resolution video reels or feeds",
                    "Live broadcast watching",
                    "Media rich stories and status updates"
                )
                AppCategory.MESSAGING -> listOf(
                    "Video calls or extended voice calls",
                    "Large video, audio, or photo downloads in chats",
                    "Chat history backup"
                )
                AppCategory.CLOUD -> listOf(
                    "Photo gallery auto-backup over cellular",
                    "Large file uploads to cloud drive",
                    "Document synchronization"
                )
                AppCategory.BROWSER -> listOf(
                    "Large file or software downloads",
                    "Multiple high-media websites opened",
                    "Embedded audio/video streaming"
                )
                AppCategory.GAMES -> listOf(
                    "In-game resource packet download",
                    "Multiplayer online gameplay session",
                    "Game patch update"
                )
                else -> listOf(
                    "General in-app media consumption",
                    "Data synchronization with online servers",
                    "Extended active screen time"
                )
            }
        }
    }
}
