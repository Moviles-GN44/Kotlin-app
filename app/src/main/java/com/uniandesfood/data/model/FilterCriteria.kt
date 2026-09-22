package com.uniandesfood.data.model

enum class BudgetRange(val label: String, val minCOP: Int, val maxCOP: Int) {
    CHEAP("$ (< 15k COP)", 0, 15000),
    MEDIUM("$$ (15k - 25k COP)", 15000, 25000),
    HIGH("$$$ (> 25k COP)", 25000, 100000)
}

data class FilterCriteria(
    val selectedBuilding: String = "ML",
    val maxWalkTimeMinutes: Float = 10f,
    val selectedBudget: BudgetRange = BudgetRange.MEDIUM,
    val isVeganSelected: Boolean = false,
    val isGlutenFreeSelected: Boolean = false,
    val isLactoseFreeSelected: Boolean = false,
    val selectedPayment: String = "Nequi / Daviplata"
)
