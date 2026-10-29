package com.uniandesfood.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.local.LocalStore
import com.uniandesfood.data.model.BudgetRange
import com.uniandesfood.data.model.FilterCriteria
import com.uniandesfood.data.model.MenuItem
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel(
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _restaurants = MutableStateFlow(restaurantRepository.getAllRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(restaurantRepository.getAllRestaurants().firstOrNull())
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(
        LocalStore.getFavorites() ?: setOf("el_toro_rgd", "one_burrito_ml")
    )
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    var currentBuilding by mutableStateOf("ML")
        private set

    private var currentCriteria: FilterCriteria? = null

    init {
        viewModelScope.launch {
            restaurantRepository.restaurantsFlow.collect { list ->
                if (currentCriteria != null) {
                    _restaurants.value = restaurantRepository.filterRestaurants(currentCriteria!!)
                } else {
                    _restaurants.value = list
                }
                if (_selectedRestaurant.value == null || !_restaurants.value.any { it.id == _selectedRestaurant.value?.id }) {
                    _selectedRestaurant.value = _restaurants.value.firstOrNull()
                }
            }
        }
    }

    fun filterRestaurants(criteria: FilterCriteria) {
        analyticsRepository.logIndividualSearch(criteria.selectedBuilding)
        currentCriteria = criteria
        currentBuilding = criteria.selectedBuilding
        val filtered = restaurantRepository.filterRestaurants(criteria)
        _restaurants.value = filtered
        _selectedRestaurant.value = filtered.firstOrNull()
    }

    // BQ8: suggests the 3 best restaurants for a group.
    // It reuses the existing filters and does not change the list shown in Explore.
    fun suggestForGroup(
        groupSize: Int,
        isVegan: Boolean,
        isGlutenFree: Boolean,
        budget: BudgetRange
    ): List<Restaurant> {
        val criteria = FilterCriteria(
            selectedBuilding = currentBuilding,
            maxWalkTimeMinutes = 15f,
            selectedBudget = budget,
            isVeganSelected = isVegan,
            isGlutenFreeSelected = isGlutenFree,
            selectedPayments = emptySet()
        )
        val matching = restaurantRepository.filterRestaurants(criteria)
        // Group size rule (our assumption: in a big group someone is more likely to lose time waiting).
        // 2-4 people: best rated first.
        // 5-8 people: drop LONG waits, then shortest wait first, then best rated.
        val suggestions = if (groupSize >= LARGE_GROUP_SIZE) {
            matching
                .filter { it.waitTimeCategory != WaitTimeCategory.LONG }
                .sortedWith(
                    compareBy<Restaurant> { it.waitTimeCategory.ordinal }
                        .thenByDescending { it.rating }
                )
                .take(3)
        } else {
            matching.sortedByDescending { it.rating }.take(3)
        }
        analyticsRepository.logGroupSuggestion(groupSize, suggestions.size, currentBuilding)
        return suggestions
    }

    fun updateCurrentBuilding(building: String) {
        currentBuilding = building
    }

    fun filterByCategory(category: String) {
        currentCriteria = null
        val filtered = restaurantRepository.filterByCategory(category)
        _restaurants.value = filtered
        _selectedRestaurant.value = filtered.firstOrNull()
    }

    fun selectRestaurant(restaurantId: String) {
        val found = restaurantRepository.getRestaurantById(restaurantId)
        _selectedRestaurant.value = found
        
        found?.let {
            analyticsRepository.logMenuInspection(
                restaurantId = it.id,
                dishCount = it.menu.size,
                checkedPhotos = false
            )
        }
    }

    fun onDishPhotoOpened(dish: MenuItem) {
        val restaurant = _selectedRestaurant.value ?: return
        analyticsRepository.logMenuInspection(
            restaurantId = restaurant.id,
            dishCount = restaurant.menu.size,
            checkedPhotos = true,
            dishId = dish.id
        )
    }

    fun toggleFavorite(restaurantId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(restaurantId)) {
            current.remove(restaurantId)
        } else {
            current.add(restaurantId)
        }
        _favorites.value = current
        LocalStore.saveFavorites(current)
    }

    fun isFavorite(restaurantId: String): Boolean = _favorites.value.contains(restaurantId)

    fun findById(id: String): Restaurant? =
        restaurantRepository.getAllRestaurants().find { it.id == id.trim() }

    private companion object {
        const val LARGE_GROUP_SIZE = 5
    }
}