package com.uniandesfood.viewmodel

import com.uniandesfood.data.repository.PopularDishesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PopularDishesViewModelTest {

    private val repository = mockk<PopularDishesRepository>(relaxed = true)
    private lateinit var viewModel: PopularDishesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = PopularDishesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun calculatePopularDishIds_ignoresDishesBelowMinimumViews() {
        val result = calculatePopularDishIds(mapOf("bp1" to 1, "bp2" to 1))

        assertTrue(result.isEmpty())
    }

    @Test
    fun calculatePopularDishIds_keepsOnlyTheTopDishes() {
        val result = calculatePopularDishIds(mapOf("bp1" to 5, "bp2" to 3, "bp3" to 2))

        assertEquals(setOf("bp1", "bp2"), result)
    }

    @Test
    fun calculatePopularDishIds_breaksTiesByDishId() {
        val result = calculatePopularDishIds(
            mapOf("bp3" to 4, "bp1" to 4, "bp2" to 4),
            maxLabels = 2
        )

        assertEquals(setOf("bp1", "bp2"), result)
    }

    @Test
    fun calculatePopularDishIds_withNoViewsReturnsEmptySet() {
        assertTrue(calculatePopularDishIds(emptyMap()).isEmpty())
    }

    @Test
    fun loadPopularDishes_exposesPopularDishesOfTheRestaurant() {
        coEvery { repository.getDishPhotoViewCounts("burger_play_rgd") } returns
            mapOf("bp1" to 3, "bp2" to 1)

        viewModel.loadPopularDishes("burger_play_rgd")

        assertEquals(setOf("bp1"), viewModel.popularDishIds.value)
    }

    @Test
    fun loadPopularDishes_withNoTelemetryShowsNoLabels() {
        coEvery { repository.getDishPhotoViewCounts("one_burrito_ml") } returns emptyMap()

        viewModel.loadPopularDishes("one_burrito_ml")

        assertTrue(viewModel.popularDishIds.value.isEmpty())
    }

    @Test
    fun loadPopularDishes_ignoresStaleResponseFromPreviousRestaurant() {
        val slowAnswerA = CompletableDeferred<Map<String, Int>>()
        coEvery { repository.getDishPhotoViewCounts("restaurant_a") } coAnswers { slowAnswerA.await() }
        coEvery { repository.getDishPhotoViewCounts("restaurant_b") } returns mapOf("b1" to 3)

        viewModel.loadPopularDishes("restaurant_a")   // se queda esperando la respuesta lenta de A
        viewModel.loadPopularDishes("restaurant_b")   // responde de inmediato para B
        slowAnswerA.complete(mapOf("a1" to 5))        // llega tarde la respuesta de A

        assertEquals(setOf("b1"), viewModel.popularDishIds.value)
    }
}