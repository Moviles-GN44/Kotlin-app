package com.uniandesfood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.BudgetRange
import com.uniandesfood.data.model.FilterCriteria
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: RestaurantViewModel? = null,
    onApplyFilters: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedBuilding by remember { mutableStateOf(viewModel?.currentBuilding ?: "ML") }
    var buildingDropdownExpanded by remember { mutableStateOf(false) }

    var walkingTime by remember { mutableStateOf("< 10 min") }
    var budgetRange by remember { mutableStateOf(5000f..25000f) }

    var isVegetarian by remember { mutableStateOf(false) }
    var isVegan by remember { mutableStateOf(false) }
    var isGlutenFree by remember { mutableStateOf(false) }

    var selectedPayments by remember { mutableStateOf(setOf("Nequi", "Cards", "Cash")) }

    val campusBuildings = listOf("ML", "RGD", "Franco", "SD", "C", "W")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Filters",
            style = MaterialTheme.typography.headlineLarge,
            color = ShadowGrey
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontally scrollable categories row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Executive Lunch", "Fast Food", "Healthy", "Desserts", "Café").forEach { category ->
                CategoryChip(
                    text = category,
                    isSelected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                        if (category == "All") {
                            viewModel?.filterByCategory("")
                        } else {
                            viewModel?.filterByCategory(category)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card containing unified filters
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // 1. Reference Campus Building
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_building),
                            contentDescription = "Building",
                            tint = UniandesAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Origin Building",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }

                    // Interactive Dropdown Button: "From ML ▾"
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UniandesAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, UniandesAmber),
                            modifier = Modifier.clickable { buildingDropdownExpanded = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "From $selectedBuilding ▾",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ShadowGrey
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = buildingDropdownExpanded,
                            onDismissRequest = { buildingDropdownExpanded = false }
                        ) {
                            campusBuildings.forEach { building ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "$building Building",
                                            fontWeight = if (selectedBuilding == building) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedBuilding == building) UniandesAmber else ShadowGrey
                                        )
                                    },
                                    onClick = {
                                        selectedBuilding = building
                                        viewModel?.updateCurrentBuilding(building)
                                        buildingDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Calculate walking distance from where you are:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    campusBuildings.forEach { building ->
                        val isSelected = selectedBuilding == building
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) UniandesAmber else BackgroundOffWhite)
                                .clickable {
                                    selectedBuilding = building
                                    viewModel?.updateCurrentBuilding(building)
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "$building Building",
                                color = if (isSelected) ShadowGrey else TextPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Walking Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_walk_time),
                        contentDescription = "Time",
                        tint = UniandesAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Max Walking Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadowGrey
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("< 5 min", "< 10 min", "< 15 min", "< 20 min").forEach { time ->
                        WalkTimeChip(
                            text = time,
                            isSelected = walkingTime == time,
                            onClick = { walkingTime = time }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Budget Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_price),
                            contentDescription = "Budget",
                            tint = UniandesAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }
                    Text(
                        text = "$${budgetRange.start.toInt().formatCop()} - $${budgetRange.endInclusive.toInt().formatCop()} COP",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = UniandesAmber
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                RangeSlider(
                    value = budgetRange,
                    onValueChange = { budgetRange = it },
                    valueRange = 5000f..35000f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = UniandesAmber,
                        activeTrackColor = UniandesAmber,
                        inactiveTrackColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Dietary Restrictions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vegan),
                        contentDescription = "Dietary",
                        tint = StatusFastGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dietary Restrictions",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadowGrey
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DietChip(text = "Vegetarian", isChecked = isVegetarian) {
                        isVegetarian = !isVegetarian
                    }
                    DietChip(text = "Vegan", isChecked = isVegan) {
                        isVegan = !isVegan
                    }
                    DietChip(text = "Gluten-Free", isChecked = isGlutenFree) {
                        isGlutenFree = !isGlutenFree
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Payment Methods
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_payment),
                        contentDescription = "Payment",
                        tint = UniandesAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Payment Methods (Multi-Select)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadowGrey
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Cards", "Nequi", "Daviplata").forEach { method ->
                        val isSelected = selectedPayments.contains(method)
                        PaymentChip(
                            text = method,
                            isSelected = isSelected,
                            onClick = {
                                val current = selectedPayments.toMutableSet()
                                if (isSelected) {
                                    if (current.size > 1) current.remove(method)
                                } else {
                                    current.add(method)
                                }
                                selectedPayments = current
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = {
                        val maxWalk = when (walkingTime) {
                            "< 5 min" -> 5f
                            "< 10 min" -> 10f
                            "< 15 min" -> 15f
                            else -> 20f
                        }
                        val budget = if (budgetRange.endInclusive <= 15000f) {
                            BudgetRange.CHEAP
                        } else if (budgetRange.endInclusive <= 25000f) {
                            BudgetRange.MEDIUM
                        } else {
                            BudgetRange.HIGH
                        }

                        val criteria = FilterCriteria(
                            selectedBuilding = selectedBuilding,
                            maxWalkTimeMinutes = maxWalk,
                            selectedBudget = budget,
                            isVeganSelected = isVegan || isVegetarian,
                            isGlutenFreeSelected = isGlutenFree,
                            selectedPayments = selectedPayments
                        )
                        viewModel?.updateCurrentBuilding(selectedBuilding)
                        viewModel?.filterRestaurants(criteria)
                        onApplyFilters()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Apply Filters",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShadowGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun Int.formatCop(): String {
    return String.format("%,d", this).replace(',', '.')
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) UniandesAmber else CardSurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) ShadowGrey else TextMuted,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun WalkTimeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CardSurfaceWhite else BackgroundOffWhite)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) UniandesAmber else TextMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun DietChip(text: String, isChecked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isChecked) StatusFastGreen else CardSurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isChecked) StatusFastGreen else BorderLight,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CardSurfaceWhite,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = if (isChecked) CardSurfaceWhite else TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun PaymentChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) UniandesAmber.copy(alpha = 0.15f) else BackgroundOffWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) UniandesAmber else TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    UniandesFoodTheme {
        ExploreScreen()
    }
}