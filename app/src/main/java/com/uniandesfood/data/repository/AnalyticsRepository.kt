package com.uniandesfood.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AnalyticsEvent(
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val params: Map<String, Any> = emptyMap()
)

class AnalyticsRepository {

    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(eventName = name, params = params)
        _events.value = _events.value + event
    }

    // Samuel's Type 2 BQ: Time spent setting up filters before selecting a restaurant
    fun logFilterSession(selectedBuilding: String, durationSeconds: Long, filtersAppliedCount: Int) {
        logEvent("filter_applied_session", mapOf(
            "building" to selectedBuilding,
            "duration_sec" to durationSeconds,
            "filters_count" to filtersAppliedCount
        ))
    }

    // Karin's Type 2 BQ: Percentage checking photo menu before visiting
    fun logMenuInspection(restaurantId: String, dishCount: Int, checkedPhotos: Boolean) {
        logEvent("menu_dish_inspected", mapOf(
            "restaurant_id" to restaurantId,
            "dish_count" to dishCount,
            "checked_photos" to checkedPhotos
        ))
    }

    // QR Review Telemetry
    fun logQrReviewSubmission(restaurantId: String, rating: Int, isCompleted: Boolean) {
        logEvent("qr_review_submitted", mapOf(
            "restaurant_id" to restaurantId,
            "rating" to rating,
            "is_completed" to isCompleted
        ))
    }
}
