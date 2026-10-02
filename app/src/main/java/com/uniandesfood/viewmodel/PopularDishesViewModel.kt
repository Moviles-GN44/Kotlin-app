package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.repository.PopularDishesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val MIN_VIEWS_FOR_POPULAR = 2
const val MAX_POPULAR_PER_RESTAURANT = 2

// A dish is "popular" when it has at least minViews photo views.
// Only the top maxLabels dishes get the label. Ties are ordered by dish id.
fun calculatePopularDishIds(
    viewCounts: Map<String, Int>,
    minViews: Int = MIN_VIEWS_FOR_POPULAR,
    maxLabels: Int = MAX_POPULAR_PER_RESTAURANT
): Set<String> =
    viewCounts.entries
        .filter { it.value >= minViews }
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .take(maxLabels)
        .map { it.key }
        .toSet()

class PopularDishesViewModel(
    private val repository: PopularDishesRepository = PopularDishesRepository()
) : ViewModel() {

    private val _popularDishIds = MutableStateFlow<Set<String>>(emptySet())
    val popularDishIds: StateFlow<Set<String>> = _popularDishIds.asStateFlow()

    private var requestedRestaurantId: String? = null

    fun loadPopularDishes(restaurantId: String) {
        requestedRestaurantId = restaurantId
        _popularDishIds.value = emptySet()
        viewModelScope.launch {
            val counts = repository.getDishPhotoViewCounts(restaurantId)
            // Ignore the answer if the student already opened another restaurant
            if (requestedRestaurantId == restaurantId) {
                _popularDishIds.value = calculatePopularDishIds(counts)
            }
        }
    }
}