package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    fun submitReview(restaurantId: String, rating: Int) {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = rating,
            isCompleted = true
        )
        viewModelScope.launch {
            restaurantRepository.submitReview(restaurantId, rating.toFloat())
        }
    }

    // Cancelled reviews are logged too, so the completion rate can be computed
    fun cancelReview(restaurantId: String) {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = 0,
            isCompleted = false
        )
    }
}