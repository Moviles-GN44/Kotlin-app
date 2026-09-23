package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import com.uniandesfood.data.model.BudgetRange
import com.uniandesfood.data.model.FilterCriteria
import com.uniandesfood.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltersViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _criteria = MutableStateFlow(FilterCriteria())
    val criteria: StateFlow<FilterCriteria> = _criteria.asStateFlow()

    private var filterSessionStartTime: Long = System.currentTimeMillis()

    fun resetSessionTimer() {
        filterSessionStartTime = System.currentTimeMillis()
    }

    fun onBuildingSelected(building: String) {
        _criteria.value = _criteria.value.copy(selectedBuilding = building)
    }

    fun onWalkTimeChanged(minutes: Float) {
        _criteria.value = _criteria.value.copy(maxWalkTimeMinutes = minutes)
    }

    fun onBudgetSelected(budget: BudgetRange) {
        _criteria.value = _criteria.value.copy(selectedBudget = budget)
    }

    fun onVeganToggled(enabled: Boolean) {
        _criteria.value = _criteria.value.copy(isVeganSelected = enabled)
    }

    fun onGlutenFreeToggled(enabled: Boolean) {
        _criteria.value = _criteria.value.copy(isGlutenFreeSelected = enabled)
    }

    fun onLactoseFreeToggled(enabled: Boolean) {
        _criteria.value = _criteria.value.copy(isLactoseFreeSelected = enabled)
    }

    fun onPaymentSelected(payment: String) {
        _criteria.value = _criteria.value.copy(selectedPayment = payment)
    }

    fun applyFilters(): FilterCriteria {
        val durationSec = ((System.currentTimeMillis() - filterSessionStartTime) / 1000).coerceAtLeast(1)
        val activeFiltersCount = listOf(
            _criteria.value.isVeganSelected,
            _criteria.value.isGlutenFreeSelected,
            _criteria.value.isLactoseFreeSelected
        ).count { it } + 2

        // Record Samuel's Type 2 BQ with complete analytical parameters
        analyticsRepository.logFilterSession(
            selectedBuilding = _criteria.value.selectedBuilding,
            durationSeconds = durationSec,
            filtersAppliedCount = activeFiltersCount,
            maxWalkTimeMinutes = _criteria.value.maxWalkTimeMinutes,
            budgetRange = _criteria.value.selectedBudget.name,
            isVegan = _criteria.value.isVeganSelected,
            isGlutenFree = _criteria.value.isGlutenFreeSelected,
            isLactoseFree = _criteria.value.isLactoseFreeSelected
        )
        return _criteria.value
    }
}
