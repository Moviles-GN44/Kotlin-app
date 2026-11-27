package com.uniandesfood.data.repository

import android.util.Log
import androidx.collection.LruCache
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.local.FileStore
import com.uniandesfood.data.model.HourlyWaitProfile
import com.uniandesfood.data.model.HourlyWaitSlot
import com.uniandesfood.data.model.PeakHoursResult
import com.uniandesfood.data.model.WaitTimeReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

/**
 * Repository for the "Best time to go" feature (Sprint 4).
 *
 * Implements:
 * - Caching: In-memory LruCache (16 entries) with a 10-minute TTL.
 * - Local Storage: Per-restaurant JSON file persisted via FileStore so the view works offline after restarts.
 * - Multi-threading: Dispatchers.IO for Firestore & disk I/O, Dispatchers.Default for hourly aggregation.
 * - Eventual Connectivity: Returns saved profile offline with freshness metadata and reloads on reconnect.
 */
class PeakHoursRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    },
    private val isOnlineProvider: () -> Boolean = { ConnectivityHelper.isOnline() },
    private val readLocalFile: (String) -> String? = { FileStore.read(it) },
    private val writeLocalFile: (String, String) -> Unit = { name, content -> FileStore.write(name, content) },
    private val currentHourProvider: () -> Int = { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) },
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    private data class CachedPeakProfile(
        val profile: HourlyWaitProfile,
        val cachedAtMillis: Long
    )

    // L1 In-Memory LRU Cache with 10-minute TTL
    private val memoryCache = LruCache<String, CachedPeakProfile>(MEMORY_CACHE_MAX_SIZE)

    suspend fun getHourlyProfile(
        restaurantId: String,
        forceRefresh: Boolean = false
    ): PeakHoursResult {
        val startNs = System.nanoTime()
        val now = nowProvider()
        val currentHour = currentHourProvider()
        val online = isOnlineProvider()

        // 1. Check L1 In-Memory LRU Cache when online and not forcing refresh
        if (!forceRefresh) {
            memoryCache.get(restaurantId)?.let { entry ->
                if (now - entry.cachedAtMillis < CACHE_TTL_MS) {
                    val recomputed = withContext(Dispatchers.Default) {
                        recomputeRecommendation(entry.profile, currentHour)
                    }
                    val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
                    return PeakHoursResult(
                        profile = recomputed,
                        fromCache = true,
                        isOffline = !online,
                        hasNeverLoadedOffline = false,
                        loadTimeMs = elapsedMs
                    )
                }
            }
        }

        // 2. Offline path: fall back to L1 memory or L2 JSON file on disk
        if (!online) {
            val savedProfile = memoryCache.get(restaurantId)?.profile
                ?: withContext(Dispatchers.IO) { readProfileFromDisk(restaurantId) }

            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            return if (savedProfile != null) {
                val recomputed = withContext(Dispatchers.Default) {
                    recomputeRecommendation(savedProfile, currentHour)
                }
                memoryCache.put(restaurantId, CachedPeakProfile(recomputed, savedProfile.savedAtMillis))
                PeakHoursResult(
                    profile = recomputed,
                    fromCache = true,
                    isOffline = true,
                    hasNeverLoadedOffline = false,
                    loadTimeMs = elapsedMs
                )
            } else {
                PeakHoursResult(
                    profile = HourlyWaitProfile(
                        restaurantId = restaurantId,
                        slots = emptyList(),
                        totalReports = 0,
                        recommendedHour = null,
                        recommendedWaitMinutes = null,
                        savedAtMillis = now
                    ),
                    fromCache = false,
                    isOffline = true,
                    hasNeverLoadedOffline = true,
                    loadTimeMs = elapsedMs
                )
            }
        }

        // 3. Online path: fetch reviews on Dispatchers.IO, aggregate on Dispatchers.Default, persist on Dispatchers.IO
        return try {
            val rawReports = withContext(Dispatchers.IO) {
                fetchWaitReportsFromFirestore(restaurantId)
            }

            val profile = withContext(Dispatchers.Default) {
                aggregateReports(restaurantId, rawReports, currentHour, now)
            }

            memoryCache.put(restaurantId, CachedPeakProfile(profile, now))
            withContext(Dispatchers.IO) {
                writeProfileToDisk(profile)
            }

            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            PeakHoursResult(
                profile = profile,
                fromCache = false,
                isOffline = false,
                hasNeverLoadedOffline = false,
                loadTimeMs = elapsedMs
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching wait reports for $restaurantId, falling back to local cache", e)
            val fallback = memoryCache.get(restaurantId)?.profile
                ?: withContext(Dispatchers.IO) { readProfileFromDisk(restaurantId) }
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            if (fallback != null) {
                val recomputed = withContext(Dispatchers.Default) {
                    recomputeRecommendation(fallback, currentHour)
                }
                PeakHoursResult(
                    profile = recomputed,
                    fromCache = true,
                    isOffline = true,
                    hasNeverLoadedOffline = false,
                    loadTimeMs = elapsedMs
                )
            } else {
                PeakHoursResult(
                    profile = HourlyWaitProfile(
                        restaurantId = restaurantId,
                        slots = emptyList(),
                        totalReports = 0,
                        recommendedHour = null,
                        recommendedWaitMinutes = null,
                        savedAtMillis = now
                    ),
                    fromCache = false,
                    isOffline = true,
                    hasNeverLoadedOffline = true,
                    loadTimeMs = elapsedMs
                )
            }
        }
    }

    /**
     * Groups wait-time reports by hour of day (0..23) and selects the quietest hour from [currentHour] onward.
     */
    fun aggregateReports(
        restaurantId: String,
        reports: List<WaitTimeReport>,
        currentHour: Int,
        nowMillis: Long = nowProvider()
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
            savedAtMillis = nowMillis
        )
    }

    private fun recomputeRecommendation(
        profile: HourlyWaitProfile,
        currentHour: Int
    ): HourlyWaitProfile {
        if (profile.slots.isEmpty()) return profile
        val upcoming = profile.slots.filter { it.hour >= currentHour }
        val candidates = if (upcoming.isNotEmpty()) upcoming else profile.slots
        val best = candidates.minWithOrNull(
            compareBy<HourlyWaitSlot> { it.averageWaitMinutes }.thenBy { it.hour }
        )
        return profile.copy(
            recommendedHour = best?.hour,
            recommendedWaitMinutes = best?.averageWaitMinutes
        )
    }

    private suspend fun fetchWaitReportsFromFirestore(restaurantId: String): List<WaitTimeReport> {
        val firestore = firestoreProvider() ?: return emptyList()
        val snapshot = firestore.collection("reviews")
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .await()

        val reports = mutableListOf<WaitTimeReport>()
        val calendar = Calendar.getInstance()

        for (doc in snapshot.documents) {
            val data = doc.data ?: continue
            val waitMinutes = parseWaitTimeMinutes(data["waitTime"]) ?: continue
            val millis = extractTimestampMillis(data["createdAt"])
                ?: extractTimestampMillis(data["timestamp"])
                ?: continue
            calendar.timeInMillis = millis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            reports.add(WaitTimeReport(hour = hour, waitMinutes = waitMinutes))
        }
        return reports
    }

    private fun writeProfileToDisk(profile: HourlyWaitProfile) {
        try {
            val slotsArray = JSONArray()
            for (slot in profile.slots) {
                slotsArray.put(
                    JSONObject().apply {
                        put("hour", slot.hour)
                        put("averageWaitMinutes", slot.averageWaitMinutes)
                        put("reportCount", slot.reportCount)
                        put("over15MinCount", slot.over15MinCount)
                    }
                )
            }
            val root = JSONObject().apply {
                put("restaurantId", profile.restaurantId)
                put("totalReports", profile.totalReports)
                put("savedAtMillis", profile.savedAtMillis)
                if (profile.recommendedHour != null) put("recommendedHour", profile.recommendedHour)
                if (profile.recommendedWaitMinutes != null) put("recommendedWaitMinutes", profile.recommendedWaitMinutes)
                put("slots", slotsArray)
            }
            writeLocalFile(cacheFileName(profile.restaurantId), root.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed writing hourly profile cache for ${profile.restaurantId}", e)
        }
    }

    private fun readProfileFromDisk(restaurantId: String): HourlyWaitProfile? {
        return try {
            val raw = readLocalFile(cacheFileName(restaurantId)) ?: return null
            val root = JSONObject(raw)
            val slotsArray = root.optJSONArray("slots") ?: JSONArray()
            val slots = mutableListOf<HourlyWaitSlot>()
            for (i in 0 until slotsArray.length()) {
                val obj = slotsArray.getJSONObject(i)
                slots.add(
                    HourlyWaitSlot(
                        hour = obj.getInt("hour"),
                        averageWaitMinutes = obj.getDouble("averageWaitMinutes"),
                        reportCount = obj.getInt("reportCount"),
                        over15MinCount = obj.optInt("over15MinCount", 0)
                    )
                )
            }
            HourlyWaitProfile(
                restaurantId = root.optString("restaurantId", restaurantId),
                slots = slots,
                totalReports = root.optInt("totalReports", slots.sumOf { it.reportCount }),
                recommendedHour = if (root.has("recommendedHour")) root.getInt("recommendedHour") else null,
                recommendedWaitMinutes = if (root.has("recommendedWaitMinutes")) root.getDouble("recommendedWaitMinutes") else null,
                savedAtMillis = root.optLong("savedAtMillis", nowProvider())
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading hourly profile cache for $restaurantId", e)
            null
        }
    }

    fun clearMemoryCache() {
        memoryCache.evictAll()
    }

    private fun cacheFileName(restaurantId: String): String = "best_time_profile_$restaurantId.json"

    companion object {
        private const val TAG = "PeakHoursRepository"
        private const val MEMORY_CACHE_MAX_SIZE = 16
        const val CACHE_TTL_MS = 10 * 60 * 1000L // 10-minute TTL

        fun parseWaitTimeMinutes(raw: Any?): Double? {
            if (raw == null) return null
            if (raw is Number) return raw.toDouble().coerceIn(0.0, 120.0)
            val normalized = raw.toString().trim().lowercase()
            return when (normalized) {
                "under5", "lessthan5", "under_5", "less_than_5", "fast", "short", "< 5 min", "<5 min", "< 5 min wait" -> 3.0
                "fivetoten", "five_to_ten", "5_10", "5-10", "5-10 min", "5-10 min wait", "moderate", "medium" -> 7.5
                "tentofifteen", "ten_to_fifteen", "10_15", "10-15", "10-15 min" -> 12.5
                "over15", "morethan15", "over_15", "more_than_15", "fifteenplus", "long", "> 15 min", ">15 min", "> 10 min wait" -> 18.0
                else -> normalized.toDoubleOrNull()?.coerceIn(0.0, 120.0)
            }
        }

        fun extractTimestampMillis(raw: Any?): Long? {
            return when (raw) {
                is Timestamp -> raw.toDate().time
                is Date -> raw.time
                is Number -> raw.toLong()
                else -> null
            }
        }
    }
}
