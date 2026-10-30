package com.uniandesfood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.model.BudgetRange
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSuggestScreen(
    viewModel: RestaurantViewModel,
    onBack: () -> Unit,
    onSelectRestaurant: (Restaurant) -> Unit
) {
    var groupSize by remember { mutableIntStateOf(4) }
    var isVegan by remember { mutableStateOf(false) }
    var isGlutenFree by remember { mutableStateOf(false) }
    var budget by remember { mutableStateOf(BudgetRange.MEDIUM) }
    
    val results by viewModel.groupSuggestions.collectAsState()
    val isSuggesting by viewModel.isSuggesting.collectAsState()
    val isOnline by ConnectivityHelper.isOnlineFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("< Back", color = TextMuted) }

        Text(
            text = "Suggest for a group",
            style = MaterialTheme.typography.headlineLarge,
            color = ShadowGrey
        )
        Text(
            text = "Walking from ${viewModel.currentBuilding}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        if (!isOnline) {
            Text(
                text = "Offline: using saved restaurants.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("How many people?", style = MaterialTheme.typography.titleMedium, color = ShadowGrey)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { if (groupSize > 2) groupSize-- }) { Text("-") }
            Text(
                text = "  $groupSize  ",
                style = MaterialTheme.typography.titleLarge,
                color = ShadowGrey
            )
            OutlinedButton(onClick = { if (groupSize < 8) groupSize++ }) { Text("+") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Dietary needs in the group", style = MaterialTheme.typography.titleMedium, color = ShadowGrey)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isVegan,
                onClick = { isVegan = !isVegan },
                label = { Text("Vegan") }
            )
            FilterChip(
                selected = isGlutenFree,
                onClick = { isGlutenFree = !isGlutenFree },
                label = { Text("Gluten-free") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Budget", style = MaterialTheme.typography.titleMedium, color = ShadowGrey)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BudgetRange.entries.forEach { option ->
                FilterChip(
                    selected = budget == option,
                    onClick = { budget = option },
                    label = { Text(option.label.substringBefore(" ")) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.suggestForGroup(groupSize, isVegan, isGlutenFree, budget)
            },
            enabled = !isSuggesting,
            colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isSuggesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = ShadowGrey,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Suggest", color = ShadowGrey)
            }
        }

        // 4. Result: top 3 restaurants
        results?.let { list ->
            Spacer(modifier = Modifier.height(20.dp))
            if (list.isEmpty()) {
                Text(
                    text = "No restaurant fits the whole group. Try a higher budget, fewer restrictions or a smaller group.",
                    color = TextMuted
                )
            } else {
                list.forEach { restaurant ->
                    val walk = restaurant.walkDistancesFromBuilding[viewModel.currentBuilding] ?: 15
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onSelectRestaurant(restaurant) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = restaurant.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = ShadowGrey
                            )
                            Text(
                                text = "${restaurant.rating} stars · $walk min walk · ${restaurant.waitTimeLabel.lowercase()} · ~\$${restaurant.averagePriceCOP} COP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}