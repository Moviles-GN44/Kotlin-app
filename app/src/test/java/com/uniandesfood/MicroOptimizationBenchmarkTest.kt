package com.uniandesfood

import com.uniandesfood.data.model.HourlyWaitProfile
import com.uniandesfood.data.model.HourlyWaitSlot
import com.uniandesfood.data.model.WaitTimeReport
import com.uniandesfood.data.repository.PeakHoursRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 4 Micro-optimization verification & timing benchmark:
 * Compares the naive 24-pass collection aggregation (Before) against the
 * single-pass primitive-array aggregation (After) in PeakHoursRepository.
 */
class MicroOptimizationBenchmarkTest {

    private fun aggregateBaseline(
        restaurantId: String,
        reports: List<WaitTimeReport>,
        currentHour: Int
    ): HourlyWaitProfile {
        val slots = (0..23).mapNotNull { hour ->
            val hourReports = reports.filter { it.hour == hour }
            if (hourReports.isEmpty()) {
                null
            } else {
                val avgWait = hourReports.map { it.waitMinutes }.average()
                val over15 = hourReports.count { it.waitMinutes > 15.0 }
                HourlyWaitSlot(
                    hour = hour,
                    averageWaitMinutes = avgWait,
                    reportCount = hourReports.size,
                    over15MinCount = over15
                )
            }
        }
        val upcoming = slots.filter { it.hour >= currentHour }
        val candidates = if (upcoming.isNotEmpty()) upcoming else slots
        val best = candidates.minWithOrNull(
            compareBy<HourlyWaitSlot> { it.averageWaitMinutes }.thenBy { it.hour }
        )
        return HourlyWaitProfile(
            restaurantId = restaurantId,
            slots = slots,
            totalReports = reports.size,
            recommendedHour = best?.hour,
            recommendedWaitMinutes = best?.averageWaitMinutes,
            savedAtMillis = 0L
        )
    }

    @Test
    fun `optimized single-pass aggregation matches baseline output and reduces execution time`() {
        val repo = PeakHoursRepository(firestoreProvider = { null })
        val hours = intArrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19)
        val waits = doubleArrayOf(3.0, 7.5, 12.5, 18.0)

        // Generate 5,000 realistic wait-time reports to measure CPU & allocation impact cleanly
        val reports = List(5_000) { idx ->
            WaitTimeReport(
                hour = hours[idx % hours.size],
                waitMinutes = waits[(idx * 7 + idx / 3) % waits.size]
            )
        }

        // Verify exact functional equivalence
        val baselineProfile = aggregateBaseline("one_burrito_ml", reports, currentHour = 12)
        val optimizedProfile = repo.aggregateReports("one_burrito_ml", reports, currentHour = 12, nowMillis = 0L)
        assertEquals(baselineProfile.totalReports, optimizedProfile.totalReports)
        assertEquals(baselineProfile.recommendedHour, optimizedProfile.recommendedHour)
        assertEquals(baselineProfile.slots.size, optimizedProfile.slots.size)
        for (i in baselineProfile.slots.indices) {
            assertEquals(baselineProfile.slots[i].hour, optimizedProfile.slots[i].hour)
            assertEquals(baselineProfile.slots[i].reportCount, optimizedProfile.slots[i].reportCount)
            assertEquals(baselineProfile.slots[i].over15MinCount, optimizedProfile.slots[i].over15MinCount)
            assertEquals(baselineProfile.slots[i].averageWaitMinutes, optimizedProfile.slots[i].averageWaitMinutes, 0.0001)
        }

        // Warm-up JIT
        repeat(50) {
            aggregateBaseline("one_burrito_ml", reports, 12)
            repo.aggregateReports("one_burrito_ml", reports, 12, 0L)
        }

        // Measure 3 independent runs (100 iterations each)
        val beforeRunsUs = LongArray(3)
        val afterRunsUs = LongArray(3)
        for (run in 0 until 3) {
            val t0 = System.nanoTime()
            repeat(100) { aggregateBaseline("one_burrito_ml", reports, 12) }
            beforeRunsUs[run] = (System.nanoTime() - t0) / 100_000L // avg microseconds per call

            val t1 = System.nanoTime()
            repeat(100) { repo.aggregateReports("one_burrito_ml", reports, 12, 0L) }
            afterRunsUs[run] = (System.nanoTime() - t1) / 100_000L // avg microseconds per call
        }

        val avgBefore = beforeRunsUs.average()
        val avgAfter = afterRunsUs.average()
        println("MICRO_OPT_BENCHMARK | Before (us): ${beforeRunsUs.joinToString(", ")} (avg=$avgBefore us)")
        println("MICRO_OPT_BENCHMARK | After  (us): ${afterRunsUs.joinToString(", ")} (avg=$avgAfter us)")
        assertTrue("Optimized implementation ($avgAfter us) should be faster than baseline ($avgBefore us)", avgAfter < avgBefore)
    }
}
