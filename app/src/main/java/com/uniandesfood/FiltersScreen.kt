package com.uniandesfood

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.BudgetRange
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.FiltersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    viewModel: FiltersViewModel? = null,
    onApplyFilters: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val criteria = viewModel?.criteria?.collectAsState()?.value

    val selectedBuilding = criteria?.selectedBuilding ?: "ML"
    val maxWalkTime = criteria?.maxWalkTimeMinutes ?: 10f
    val selectedBudget = criteria?.selectedBudget ?: BudgetRange.MEDIUM
    val isVeganSelected = criteria?.isVeganSelected ?: false
    val isGlutenFreeSelected = criteria?.isGlutenFreeSelected ?: false
    val selectedPayments = criteria?.selectedPayments ?: setOf("Nequi", "Cards", "Cash")

    LaunchedEffect(Unit) {
        viewModel?.resetSessionTimer()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search & Dining Filters",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShadowGrey
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = StatusLongRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurfaceWhite)
            )
        },
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Reference Campus Building
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            text = "Reference Campus Building",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
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
                        listOf("ML", "RGD", "Franco", "SD", "C", "W").forEach { building ->
                            FilterChip(
                                selected = selectedBuilding == building,
                                onClick = {
                                    viewModel?.onBuildingSelected(building)
                                },
                                label = { Text(building, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UniandesAmber,
                                    selectedLabelColor = ShadowGrey
                                )
                            )
                        }
                    }
                }
            }

            // 2. Max Walking Time
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                painter = painterResource(id = R.drawable.ic_walk_time),
                                contentDescription = "Walk Time",
                                tint = UniandesAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Max Walking Time",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShadowGrey
                            )
                        }
                        Text(
                            text = "< ${maxWalkTime.toInt()} min",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                            color = UniandesAmber
                        )
                    }
                    Slider(
                        value = maxWalkTime,
                        onValueChange = {
                            viewModel?.onWalkTimeChanged(it)
                        },
                        valueRange = 3f..25f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = UniandesAmber,
                            activeTrackColor = UniandesAmber
                        )
                    )
                    Text(
                        text = "Estimated walking time from $selectedBuilding building",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // 3. Student Budget
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            text = "Student Budget Range",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BudgetRange.values().forEach { budget ->
                            FilterChip(
                                selected = selectedBudget == budget,
                                onClick = {
                                    viewModel?.onBudgetSelected(budget)
                                },
                                label = { Text(budget.label, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UniandesAmber,
                                    selectedLabelColor = ShadowGrey
                                )
                            )
                        }
                    }
                }
            }

            // 4. Dietary Restrictions
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_vegan),
                            contentDescription = "Dietary",
                            tint = MintEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Dietary & Health Restrictions",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

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
                                painter = painterResource(id = R.drawable.ic_vegan_filled),
                                contentDescription = "Vegan",
                                tint = MintEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Vegan / Vegetarian Options",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Switch(
                            checked = isVeganSelected,
                            onCheckedChange = {
                                viewModel?.onVeganToggled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintEmerald,
                                checkedTrackColor = MintEmerald.copy(alpha = 0.5f)
                            )
                        )
                    }

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
                                painter = painterResource(id = R.drawable.ic_allergen_filled),
                                contentDescription = "Gluten Free",
                                tint = TagAllergenPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Gluten-Free / Celiac Safe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Switch(
                            checked = isGlutenFreeSelected,
                            onCheckedChange = {
                                viewModel?.onGlutenFreeToggled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintEmerald,
                                checkedTrackColor = MintEmerald.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // 5. Accepted Payment Methods (Multi-Select)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            text = "Preferred Payment Methods (Multi-Select)",
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
                        listOf("Nequi", "Daviplata", "Cards", "Cash").forEach { payment ->
                            val isSelected = selectedPayments.contains(payment)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel?.onPaymentToggled(payment)
                                },
                                label = { Text(payment, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UniandesAmber,
                                    selectedLabelColor = ShadowGrey
                                )
                            )
                        }
                    }
                }
            }

            // Apply Button
            Button(
                onClick = {
                    viewModel?.applyFilters()
                    onApplyFilters()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Apply Filters",
                    style = MaterialTheme.typography.labelLarge,
                    color = ShadowGrey
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FiltersScreenPreview() {
    UniandesFoodTheme {
        FiltersScreen()
    }
}
