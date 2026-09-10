package com.example.data.repository

import com.example.data.model.AppCategory
import com.example.data.model.AppUsageItem
import com.example.data.model.InvestigationResult
import com.example.data.model.TimeRange
import com.example.util.DataFormatUtils

class InvestigationRepository(private val networkStatsRepository: NetworkStatsRepository) {

    suspend fun runInvestigation(topic: String = "My data disappeared quickly"): InvestigationResult {
        // Query today's usage as the primary focus
        val apps = networkStatsRepository.getUsageData(TimeRange.TODAY)
        val totalMobileBytes = apps.sumOf { it.mobileBytes }
        val topApps = apps.sortedByDescending { it.mobileBytes }.take(5)

        val biggestApp = topApps.firstOrNull()
        val backgroundCulprit = apps.firstOrNull { it.hasBackgroundDominance && it.backgroundBytes > 25 * 1024 * 1024L }

        val explanations = mutableListOf<String>()
        var recommendedAction = "Keep monitoring your usage. No severe anomalies detected."
        var targetPkg: String? = null

        if (biggestApp != null) {
            val formatted = DataFormatUtils.formatBytes(biggestApp.mobileBytes)
            explanations.add("FACT: ${biggestApp.appName} was your largest mobile-data user, consuming $formatted.")
            targetPkg = biggestApp.packageName

            when (biggestApp.category) {
                AppCategory.VIDEO -> {
                    explanations.add("POSSIBLE REASON: Video streaming at high resolution (HD/4K) or autoplay feeds can consume 500MB to 1GB per hour.")
                    recommendedAction = "Consider lowering video resolution to 480p or saving videos over Wi-Fi."
                }
                AppCategory.SOCIAL -> {
                    explanations.add("POSSIBLE REASON: Social media feeds with auto-playing videos and high-res reels.")
                    recommendedAction = "Enable in-app 'Data Saver' inside ${biggestApp.appName}'s media settings."
                }
                AppCategory.CLOUD -> {
                    explanations.add("POSSIBLE REASON: Cloud media upload or continuous camera roll synchronization.")
                    recommendedAction = "Switch ${biggestApp.appName}'s backup setting to 'Over Wi-Fi Only'."
                }
                AppCategory.MESSAGING -> {
                    explanations.add("POSSIBLE REASON: Video calls or automatic download of videos in group chats.")
                    recommendedAction = "Turn off auto-download for videos and documents in chat settings."
                }
                else -> {
                    explanations.add("POSSIBLE REASON: Active continuous background server synchronization or download tasks.")
                    recommendedAction = "Review data permissions and background activity in Android settings."
                }
            }
        }

        if (backgroundCulprit != null) {
            val bgFormatted = DataFormatUtils.formatBytes(backgroundCulprit.backgroundBytes)
            explanations.add("OBSERVATION: ${backgroundCulprit.appName} used $bgFormatted while running silently in the background.")
            recommendedAction = "Restrict background data for ${backgroundCulprit.appName} in Android App Settings."
            targetPkg = backgroundCulprit.packageName
        }

        val title = when (topic) {
            "I want to find background usage" -> "Background Data Audit"
            "An app used too much data" -> "Top App Usage Report"
            "My data is finishing too early" -> "Runway Pacing Analysis"
            else -> "Investigation Result"
        }

        val summary = if (biggestApp != null) {
            "Your mobile data increased by ${DataFormatUtils.formatBytes(totalMobileBytes)} today. ${biggestApp.appName} accounted for most of this consumption."
        } else {
            "Your mobile-data consumption is currently minimal."
        }

        return InvestigationResult(
            title = title,
            summary = summary,
            totalBytesUsed = totalMobileBytes,
            topApps = topApps,
            biggestContributor = biggestApp,
            backgroundCulprit = backgroundCulprit,
            explanations = explanations,
            recommendedAction = recommendedAction,
            targetPackageName = targetPkg
        )
    }
}
