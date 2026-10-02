package com.uniandesfood.viewmodel

import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val analyticsRepository = mockk<AnalyticsRepository>(relaxed = true)
    private val restaurantRepository = mockk<RestaurantRepository>(relaxed = true)
    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setUp() {
        // ViewModel needs a Main dispatcher, which does not exist in JVM unit tests
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ReviewViewModel(
            analyticsRepository = analyticsRepository,
            restaurantRepository = restaurantRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitReview_logsCompletedReviewWithRating() {
        viewModel.submitReview(restaurantId = "one_burrito_ml", rating = 4)

        verify(exactly = 1) {
            analyticsRepository.logQrReviewSubmission(
                restaurantId = "one_burrito_ml",
                rating = 4,
                isCompleted = true
            )
        }
    }

    @Test
    fun cancelReview_logsIncompleteReviewWithZeroRating() {
        viewModel.cancelReview(restaurantId = "one_burrito_ml")

        verify(exactly = 1) {
            analyticsRepository.logQrReviewSubmission(
                restaurantId = "one_burrito_ml",
                rating = 0,
                isCompleted = false
            )
        }
    }
}