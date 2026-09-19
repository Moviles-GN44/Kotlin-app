package com.uniandesfood

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
import com.uniandesfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onApplyFilters: () -> Unit = {}
) {
    var selectedBuilding by remember { mutableStateOf("ML") }
    var maxWalkTime by remember { mutableFloatStateOf(10f) }
    var selectedBudget by remember { mutableStateOf("$$ (15k - 25k COP)") }
    var isVeganSelected by remember { mutableStateOf(false) }
    var isGlutenFreeSelected by remember { mutableStateOf(false) }
    var isLactoseFreeSelected by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Nequi / Daviplata") }

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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ML", "RGD", "Franco", "C", "W").forEach { building ->
                            FilterChip(
                                selected = selectedBuilding == building,
                                onClick = { selectedBuilding = building },
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
                        onValueChange = { maxWalkTime = it },
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
                        listOf(
                            "$ (< 15k COP)",
                            "$$ (15k - 25k COP)",
                            "$$$ (> 25k COP)"
                        ).forEach { budget ->
                            FilterChip(
                                selected = selectedBudget == budget,
                                onClick = { selectedBudget = budget },
                                label = { Text(budget, style = MaterialTheme.typography.bodySmall) },
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
                            onCheckedChange = { isVeganSelected = it },
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
                            onCheckedChange = { isGlutenFreeSelected = it },
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
                                painter = painterResource(id = R.drawable.ic_allergen),
                                contentDescription = "Lactose Free",
                                tint = TagAllergenPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Lactose-Free",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Switch(
                            checked = isLactoseFreeSelected,
                            onCheckedChange = { isLactoseFreeSelected = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintEmerald,
                                checkedTrackColor = MintEmerald.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // 5. Accepted Payment Methods
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
                            text = "Preferred Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Nequi / Daviplata", "Cards", "Cash").forEach { payment ->
                            FilterChip(
                                selected = selectedPayment == payment,
                                onClick = { selectedPayment = payment },
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
                onClick = onApplyFilters,
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
