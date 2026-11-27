package com.uniandesfood.data.model

import kotlin.math.roundToInt

/**
 * Aggregated waiting time statistics for a single hour of the day (0..23).
 */
data class HourlyWaitSlot(
    val hour: Int,
    val averageWaitMinutes: Double,
    val reportCount: Int,
    val over15MinCount: Int = 0
) {
    val hourLabel: String
        get() = "%02d:00".format(hour)

    val waitCategory: WaitTimeCategory
        get() = when {
            averageWaitMinutes < 7.5 -> WaitTimeCategory.FAST
            averageWaitMinutes <= 15.0 -> WaitTimeCategory.MODERATE
            else -> WaitTimeCategory.LONG
        }

    val waitLabel: String
        get() = when {
            averageWaitMinutes < 5.0 -> "< 5 min"
            averageWaitMinutes <= 10.0 -> "5-10 min"
            averageWaitMinutes <= 15.0 -> "10-15 min"
            else -> "> 15 min"
        }

    val roundedMinutes: Int
        get() = averageWaitMinutes.roundToInt()

    val congestionSharePct: Int
        get() = if (reportCount == 0) 0 else ((over15MinCount.toDouble() / reportCount) * 100.0).roundToInt()
}

/**
 * Complete hourly wait-time profile for a restaurant, including the
 * recommended quietest upcoming hour.
 */
data class HourlyWaitProfile(
    val restaurantId: String,
    val slots: List<HourlyWaitSlot>,
    val totalReports: Int,
    val recommendedHour: Int?,
    val recommendedWaitMinutes: Double?,
    val savedAtMillis: Long = System.currentTimeMillis()
) {
    val recommendedHourLabel: String?
        get() = recommendedHour?.let { "%02d:00".format(it) }

    val recommendedWaitLabel: String?
        get() = recommendedWaitMinutes?.let { avg ->
            when {
                avg < 5.0 -> "< 5 min wait"
                avg <= 10.0 -> "5-10 min wait"
                avg <= 15.0 -> "10-15 min wait"
                else -> "> 15 min wait"
            }
        }
}

/**
 * Lightweight raw wait-time report extracted from Firestore reviews.
 */
data class WaitTimeReport(
    val hour: Int,
    val waitMinutes: Double
)

/**
 * Result returned by PeakHoursRepository describing data freshness and offline state.
 */
data class PeakHoursResult(
    val profile: HourlyWaitProfile,
    val fromCache: Boolean,
    val isOffline: Boolean,
    val hasNeverLoadedOffline: Boolean,
    val loadTimeMs: Long
)
