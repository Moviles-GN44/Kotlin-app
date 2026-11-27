package com.uniandesfood

import com.uniandesfood.data.model.WaitTimeReport
import com.uniandesfood.data.repository.PeakHoursRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PeakHoursRepositoryTest {

    @Test
    fun `aggregateReports groups by hour and recommends quietest upcoming hour`() {
        val repo = PeakHoursRepository(firestoreProvider = { null })
        val reports = listOf(
            WaitTimeReport(hour = 11, waitMinutes = 3.0),
            WaitTimeReport(hour = 12, waitMinutes = 18.0),
            WaitTimeReport(hour = 12, waitMinutes = 18.0),
            WaitTimeReport(hour = 13, waitMinutes = 12.5),
            WaitTimeReport(hour = 14, waitMinutes = 3.0),
            WaitTimeReport(hour = 14, waitMinutes = 7.5)
        )

        val profile = repo.aggregateReports(
            restaurantId = "one_burrito_ml",
            reports = reports,
            currentHour = 12,
            nowMillis = 1000L
        )

        assertEquals(6, profile.totalReports)
        assertEquals(4, profile.slots.size)
        // Hour 11 is in the past relative to currentHour=12, so quietest upcoming hour is 14 (avg 5.25 min)
        assertEquals(14, profile.recommendedHour)
        assertEquals(5.25, profile.recommendedWaitMinutes!!, 0.01)
    }

    @Test
    fun `offline mode falls back to local disk json file`() = runTest {
        val disk = mutableMapOf<String, String>()
        val onlineRepo = PeakHoursRepository(
            firestoreProvider = { null },
            isOnlineProvider = { true },
            readLocalFile = { disk[it] },
            writeLocalFile = { k, v -> disk[k] = v },
            currentHourProvider = { 12 }
        )

        // Seed disk directly as if loaded earlier online
        disk["best_time_profile_el_toro_rgd.json"] = """
            {
              "restaurantId": "el_toro_rgd",
              "totalReports": 3,
              "savedAtMillis": 1000,
              "recommendedHour": 15,
              "recommendedWaitMinutes": 3.0,
              "slots": [
                {"hour": 13, "averageWaitMinutes": 18.0, "reportCount": 2, "over15MinCount": 2},
                {"hour": 15, "averageWaitMinutes": 3.0, "reportCount": 1, "over15MinCount": 0}
              ]
            }
        """.trimIndent()

        val offlineRepo = PeakHoursRepository(
            firestoreProvider = { null },
            isOnlineProvider = { false },
            readLocalFile = { disk[it] },
            writeLocalFile = { k, v -> disk[k] = v },
            currentHourProvider = { 12 }
        )

        val result = offlineRepo.getHourlyProfile("el_toro_rgd")
        assertTrue(result.isOffline)
        assertTrue(result.fromCache)
        assertFalse(result.hasNeverLoadedOffline)
        assertEquals(15, result.profile.recommendedHour)
        assertEquals(2, result.profile.slots.size)
        // Ensure onlineRepo compiles and is clean
        assertNotNull(onlineRepo)
    }

    @Test
    fun `offline mode with no cached file sets hasNeverLoadedOffline`() = runTest {
        val offlineRepo = PeakHoursRepository(
            firestoreProvider = { null },
            isOnlineProvider = { false },
            readLocalFile = { null },
            writeLocalFile = { _, _ -> }
        )

        val result = offlineRepo.getHourlyProfile("unknown_spot")
        assertTrue(result.isOffline)
        assertTrue(result.hasNeverLoadedOffline)
        assertTrue(result.profile.slots.isEmpty())
    }

    @Test
    fun `parseWaitTimeMinutes supports both Flutter and Kotlin wait time values`() {
        assertEquals(3.0, PeakHoursRepository.parseWaitTimeMinutes("under5")!!, 0.01)
        assertEquals(10.0, PeakHoursRepository.parseWaitTimeMinutes("fiveTo15")!!, 0.01)
        assertEquals(7.5, PeakHoursRepository.parseWaitTimeMinutes("fiveToTen")!!, 0.01)
        assertEquals(12.5, PeakHoursRepository.parseWaitTimeMinutes("tenToFifteen")!!, 0.01)
        assertEquals(18.0, PeakHoursRepository.parseWaitTimeMinutes("over15")!!, 0.01)
        assertEquals(3.0, PeakHoursRepository.parseWaitTimeMinutes("< 5 min")!!, 0.01)
        assertEquals(18.0, PeakHoursRepository.parseWaitTimeMinutes("> 15 min")!!, 0.01)
    }

    @Test
    fun `in-memory LruCache serves fresh entry within 10-minute TTL`() = runTest {
        var now = 1_000_000L
        val disk = mutableMapOf<String, String>()
        val repo = PeakHoursRepository(
            firestoreProvider = { null },
            isOnlineProvider = { true },
            readLocalFile = { disk[it] },
            writeLocalFile = { k, v -> disk[k] = v },
            currentHourProvider = { 12 },
            nowProvider = { now }
        )

        val first = repo.getHourlyProfile("one_burrito_ml")
        assertFalse(first.fromCache)

        // 5 minutes later -> must hit L1 LruCache
        now += 5 * 60 * 1000L
        val second = repo.getHourlyProfile("one_burrito_ml")
        assertTrue(second.fromCache)

        // 11 minutes after first load -> TTL expired, must reload
        now += 6 * 60 * 1000L
        val third = repo.getHourlyProfile("one_burrito_ml")
        assertFalse(third.fromCache)
    }
}
