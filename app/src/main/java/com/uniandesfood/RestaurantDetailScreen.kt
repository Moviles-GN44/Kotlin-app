package com.uniandesfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.MenuItem
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: RestaurantViewModel? = null,
    onBack: () -> Unit = {},
    onScanQR: () -> Unit = {}
) {
    val selectedRestaurant = viewModel?.selectedRestaurant?.collectAsState()?.value

    val currentBuilding = viewModel?.currentBuilding ?: (selectedRestaurant?.buildingTag ?: "ML")
    val walkMinutes = selectedRestaurant?.walkDistancesFromBuilding?.get(currentBuilding) ?: 2
    val restaurantName = selectedRestaurant?.name ?: "One Burrito - ML"
    val waitTimeLabel = selectedRestaurant?.waitTimeLabel ?: "< 5 MIN WAIT"
    val waitTimeCategory = selectedRestaurant?.waitTimeCategory ?: WaitTimeCategory.FAST
    val ratingText = "${selectedRestaurant?.rating ?: 4.7} (${selectedRestaurant?.reviewCount ?: 128} verified student reviews)"
    val walkTimeText = "$walkMinutes min walk from $currentBuilding Building"
    val paymentsText = "Accepts: ${(selectedRestaurant?.paymentMethods ?: listOf("Nequi", "Daviplata", "Cards", "Cash")).joinToString(", ")}"

    val menuDishes = selectedRestaurant?.menu?.ifEmpty { null } ?: listOf(
        MenuItem(
            id = "d1",
            name = "Criollo Student Bowl",
            description = "Rice, red beans, sweet plantains, grilled chicken & fresh garden salad.",
            priceCOP = 15500,
            formattedPrice = "$15,500 COP",
            isVegan = false,
            isGlutenFree = false
        ),
        MenuItem(
            id = "d2",
            name = "Express Mixed Burrito",
            description = "Artisanal flour tortilla with seasoned shredded beef, guacamole & pico de gallo.",
            priceCOP = 17000,
            formattedPrice = "$17,000 COP",
            isVegan = false,
            isGlutenFree = false
        ),
        MenuItem(
            id = "d3",
            name = "Quinoa & Avocado Bowl",
            description = "Fresh mixed greens, crispy quinoa, cherry tomatoes & tahini-lime dressing.",
            priceCOP = 16000,
            formattedPrice = "$16,000 COP",
            isVegan = true,
            isGlutenFree = true
        ),
        MenuItem(
            id = "d4",
            name = "Coffee & Baked Empanada Combo",
            description = "8oz hot Americano coffee with baked spinach & ricotta empanada.",
            priceCOP = 7500,
            formattedPrice = "$7,500 COP",
            isVegan = false,
            isGlutenFree = false
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Restaurant Details",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShadowGrey
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurfaceWhite)
            )
        },
        bottomBar = {
            Surface(
                color = CardSurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onScanQR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_review),
                            contentDescription = "Scan QR",
                            tint = ShadowGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Scan In-Store QR & Review",
                            style = MaterialTheme.typography.labelLarge,
                            color = ShadowGrey
                        )
                    }
                }
            }
        },
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Restaurant Header Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
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
                            Text(
                                text = restaurantName,
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp),
                                color = ShadowGrey
                            )
                            // Traffic-light Wait Time Badge
                            val statusColor = when (waitTimeCategory) {
                                WaitTimeCategory.FAST -> StatusFastGreen
                                WaitTimeCategory.MODERATE -> StatusModerateAmber
                                WaitTimeCategory.LONG -> StatusLongRed
                            }
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_wait_fast_filled),
                                        contentDescription = "Wait time queue",
                                        tint = statusColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = waitTimeLabel,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_rating_filled),
                                contentDescription = "Rating",
                                tint = UniandesAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = ratingText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = UniandesAmber
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_verified),
                                contentDescription = "Verified",
                                tint = MintEmerald,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_walk_time),
                                contentDescription = "Walk Time",
                                tint = UniandesAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = walkTimeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_payment),
                                contentDescription = "Payment Methods",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = paymentsText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Menu Section Header
            item {
                Text(
                    text = "Available Menu Dishes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShadowGrey
                )
            }

            // Menu List
            items(menuDishes) { dish ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = dish.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = ShadowGrey
                            )

                            // Clean Dietary Badges Row with vector icons
                            if (dish.isVegan || dish.isGlutenFree) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (dish.isVegan) {
                                        Surface(
                                            color = MintEmerald.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_vegan_filled),
                                                    contentDescription = "Vegan",
                                                    tint = MintEmerald,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "VEGAN",
                                                    color = MintEmerald,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                    if (dish.isGlutenFree) {
                                        Surface(
                                            color = TagAllergenPurple.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_allergen_filled),
                                                    contentDescription = "Gluten Free",
                                                    tint = TagAllergenPurple,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "GLUTEN-FREE",
                                                    color = TagAllergenPurple,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dish.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 2
                            )
                        }

                        Text(
                            text = dish.formattedPrice,
                            style = MaterialTheme.typography.titleLarge,
                            color = UniandesAmber
                        )
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantDetailScreenPreview() {
    UniandesFoodTheme {
        RestaurantDetailScreen()
    }
}
