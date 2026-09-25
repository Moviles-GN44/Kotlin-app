package com.uniandesfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class AnalyticsEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String = "Android-Kotlin",
    val params: Map<String, Any> = emptyMap()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "eventId" to eventId,
            "eventName" to eventName,
            "timestamp" to timestamp,
            "platform" to platform,
            "params" to params
        )
    }
}

class AnalyticsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Optional remote microservice endpoint URL configured by group
    private var remoteEndpointUrl: String? = null

    fun setRemoteEndpointUrl(url: String) {
        this.remoteEndpointUrl = url
    }

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(
            eventName = name,
            params = params
        )
        _events.value = _events.value + event

        // Asynchronously persist event to Cloud Firestore telemetry collection
        coroutineScope.launch {
            persistEventToFirestore(event)
            remoteEndpointUrl?.let { url ->
                sendEventToHttpEndpoint(url, event)
            }
        }
    }

    private fun persistEventToFirestore(event: AnalyticsEvent) {
        try {
            firestore.collection("telemetry_events")
                .document(event.eventId)
                .set(event.toMap(), SetOptions.merge())
        } catch (_: Exception) {
            // Graceful offline fallback
        }
    }

    private fun sendEventToHttpEndpoint(endpointUrl: String, event: AnalyticsEvent) {
        try {
            val url = URL(endpointUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val jsonObject = JSONObject(event.toMap())
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonObject.toString())
                writer.flush()
            }
            conn.responseCode // Trigger execution
            conn.disconnect()
        } catch (_: Exception) {
            // Telemetry should never crash the user experience
        }
    }

    /**
     * Samuel's Type 2 BQ:
     * Analyzes latency / duration spent setting up filters, selected campus reference building,
     * walking time limits, budget, and dietary restrictions before finding dining options.
     */
    fun logFilterSession(
        selectedBuilding: String,
        durationSeconds: Long,
        filtersAppliedCount: Int,
        maxWalkTimeMinutes: Float = 10f,
        budgetRange: String = "MEDIUM",
        isVegan: Boolean = false,
        isGlutenFree: Boolean = false,
        isLactoseFree: Boolean = false
    ) {
        logEvent(
            name = "samuel_bq_filter_session",
            params = mapOf(
                "bq_author" to "Samuel",
                "building" to selectedBuilding,
                "duration_sec" to durationSeconds,
                "filters_count" to filtersAppliedCount,
                "max_walk_time_min" to maxWalkTimeMinutes,
                "budget_range" to budgetRange,
                "is_vegan" to isVegan,
                "is_gluten_free" to isGlutenFree,
                "is_lactose_free" to isLactoseFree
            )
        )
    }

    /**
     * Karin's Type 2 BQ:
     * Tracks student engagement with photographic menus and dish inspection.
     */
    fun logMenuInspection(restaurantId: String, dishCount: Int, checkedPhotos: Boolean) {
        logEvent(
            name = "karin_bq_menu_inspection",
            params = mapOf(
                "bq_author" to "Karin",
                "restaurant_id" to restaurantId,
                "dish_count" to dishCount,
                "checked_photos" to checkedPhotos
            )
        )
    }

    /**
     * In-Store QR Review Submission Telemetry
     */
    fun logQrReviewSubmission(restaurantId: String, rating: Int, isCompleted: Boolean) {
        logEvent(
            name = "qr_review_submission",
            params = mapOf(
                "restaurant_id" to restaurantId,
                "rating" to rating,
                "is_completed" to isCompleted
            )
        )
    }
}
