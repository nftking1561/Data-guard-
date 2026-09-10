package com.example.data.model

enum class BudgetMode(val displayName: String, val description: String) {
    FIXED(
        displayName = "Fixed Target",
        description = "A constant daily target based on your total plan divided by total days."
    ),
    SMART(
        displayName = "Smart Flexibility",
        description = "Unused allowance today gives you more room to use data comfortably tomorrow."
    )
}

enum class TimeRange(val title: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days");

    fun getStartAndEndEpochMs(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        // Set to start of today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        return when (this) {
            TODAY -> Pair(startOfToday, now)
            YESTERDAY -> {
                val startOfYesterday = startOfToday - 24L * 60 * 60 * 1000
                Pair(startOfYesterday, startOfToday)
            }
            LAST_7_DAYS -> {
                val sevenDaysAgo = startOfToday - 6L * 24 * 60 * 60 * 1000
                Pair(sevenDaysAgo, now)
            }
            LAST_30_DAYS -> {
                val thirtyDaysAgo = startOfToday - 29L * 24 * 60 * 60 * 1000
                Pair(thirtyDaysAgo, now)
            }
        }
    }
}
