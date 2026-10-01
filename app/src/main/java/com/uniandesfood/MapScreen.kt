package com.uniandesfood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: RestaurantViewModel? = null,
    onViewRestaurant: (Restaurant) -> Unit = {},
    onOpenFilters: () -> Unit = {}
) {
    val restaurants = viewModel?.restaurants?.collectAsState()?.value ?: emptyList()
    val selectedRestaurant = viewModel?.selectedRestaurant?.collectAsState()?.value ?: restaurants.firstOrNull()
    val favorites = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val currentBuilding = viewModel?.currentBuilding ?: "ML"

    var searchQuery by remember { mutableStateOf("") }

    // Map positions relative to campus canvas [0f..1f, 0f..1f]
    val restaurantMapCoords = remember {
        mapOf(
            "one_burrito_ml" to Offset(0.32f, 0.28f),      // ML Building Area
            "el_toro_rgd" to Offset(0.54f, 0.52f),         // RGD Area
            "one_burrito_rgd" to Offset(0.68f, 0.48f),     // RGD East
            "burger_play_rgd" to Offset(0.50f, 0.62f),     // RGD South
            "la_cabra_sanduchera_rgd" to Offset(0.60f, 0.68f), // RGD Plaza
            "burger_play_sd" to Offset(0.25f, 0.78f),      // SD Building Area
            "la_liebre_franco" to Offset(0.78f, 0.35f)     // Franco Area
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
    ) {
        // 1. Campus Interactive Map Canvas
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            // Campus Base Map Drawing
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Find closest restaurant to tap
                            val tappedRest = restaurants.minByOrNull { rest ->
                                val normCoord = restaurantMapCoords[rest.id] ?: Offset(0.5f, 0.5f)
                                val markerX = normCoord.x * widthPx
                                val markerY = normCoord.y * (heightPx * 0.72f) + (heightPx * 0.12f)
                                val dx = offset.x - markerX
                                val dy = offset.y - markerY
                                (dx * dx) + (dy * dy)
                            }
                            if (tappedRest != null) {
                                viewModel?.selectRestaurant(tappedRest.id)
                            }
                        }
                    }
            ) {
                // Background Campus Zone
                drawRect(
                    color = Color(0xFFF1F5F9), // Light campus slate
                    size = size
                )

                // Green Park / Central Campus Gardens
                drawRoundRect(
                    color = Color(0xFFDCFCE7), // Mint green park
                    topLeft = Offset(widthPx * 0.18f, heightPx * 0.22f),
                    size = Size(widthPx * 0.64f, heightPx * 0.44f),
                    cornerRadius = CornerRadius(24f, 24f)
                )

                // Campus Pathways (Walkways)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(widthPx * 0.10f, heightPx * 0.30f),
                    end = Offset(widthPx * 0.90f, heightPx * 0.60f),
                    strokeWidth = 14f
                )
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(widthPx * 0.32f, heightPx * 0.18f),
                    end = Offset(widthPx * 0.55f, heightPx * 0.75f),
                    strokeWidth = 12f
                )

                // Campus Buildings Outlines & Labels (ML, RGD, Franco, SD, C, W)
                val buildings = listOf(
                    Triple(Offset(widthPx * 0.24f, heightPx * 0.22f), Size(widthPx * 0.22f, heightPx * 0.12f), "Edificio ML"),
                    Triple(Offset(widthPx * 0.48f, heightPx * 0.46f), Size(widthPx * 0.26f, heightPx * 0.14f), "Edificio RGD"),
                    Triple(Offset(widthPx * 0.72f, heightPx * 0.28f), Size(widthPx * 0.22f, heightPx * 0.12f), "Edificio Franco"),
                    Triple(Offset(widthPx * 0.15f, heightPx * 0.72f), Size(widthPx * 0.25f, heightPx * 0.12f), "Edificio SD")
                )

                for ((bTopLeft, bSize, bName) in buildings) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    drawRoundRect(
                        color = Color(0xFFCBD5E1),
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = CornerRadius(16f, 16f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                    )
                }
            }

            // Restaurant Pins Overlay
            restaurants.forEach { rest ->
                val normCoord = restaurantMapCoords[rest.id] ?: Offset(0.5f, 0.5f)
                val isSelected = rest.id == selectedRestaurant?.id
                val xPos = normCoord.x * widthPx
                val yPos = normCoord.y * (heightPx * 0.70f) + (heightPx * 0.12f)

                Box(
                    modifier = Modifier
                        .offset(
                            x = (xPos - 50).dp.coerceAtLeast(10.dp),
                            y = (yPos - 25).dp.coerceAtLeast(100.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) UniandesAmber else CardSurfaceWhite)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) UniandesAmber else BorderLight,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .shadow(elevation = if (isSelected) 8.dp else 2.dp, shape = RoundedCornerShape(20.dp))
                        .clickable { viewModel?.selectRestaurant(rest.id) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = rest.name,
                            tint = if (isSelected) ShadowGrey else UniandesAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = rest.name.replace(" - RGD", "").replace(" - ML", "").replace(" - Franco", "").replace(" - SD", ""),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) ShadowGrey else TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Top Campus Search Bar & Filter Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceWhite,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Buscar en el campus Uniandes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    // Filter Action Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = UniandesAmber.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { onOpenFilters() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_explore),
                            contentDescription = "Filters",
                            tint = UniandesAmber,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // 3. Floating Bottom Restaurant Card (Matching Flutter video design)
        if (selectedRestaurant != null) {
            val isFav = favorites.contains(selectedRestaurant.id)
            val walkMin = selectedRestaurant.walkDistancesFromBuilding[currentBuilding] ?: 2
            val statusColor = when (selectedRestaurant.waitTimeCategory) {
                WaitTimeCategory.FAST -> StatusFastGreen
                WaitTimeCategory.MODERATE -> StatusModerateAmber
                WaitTimeCategory.LONG -> StatusLongRed
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title + Building + Favorite Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedRestaurant.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = ShadowGrey,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedRestaurant.category} • Edificio ${selectedRestaurant.buildingTag}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            // Favorite Heart Toggle Button
                            IconButton(
                                onClick = { viewModel?.toggleFavorite(selectedRestaurant.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) StatusLongRed else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Badges Row (Walk time, Rating, Wait queue)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Walk time badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundOffWhite
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_walk_time),
                                        contentDescription = "Walk",
                                        tint = UniandesAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "$walkMin min",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ShadowGrey
                                    )
                                }
                            }

                            // Rating badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundOffWhite
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_rating_filled),
                                        contentDescription = "Rating",
                                        tint = UniandesAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (selectedRestaurant.reviewCount == 0) "N/A ★ (0)" else "${selectedRestaurant.rating} ★ (${selectedRestaurant.reviewCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ShadowGrey
                                    )
                                }
                            }

                            // Wait time traffic badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedRestaurant.waitTimeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Orange CTA Button: "Ver Menú y Detalles"
                        Button(
                            onClick = { onViewRestaurant(selectedRestaurant) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu),
                                    contentDescription = null,
                                    tint = ShadowGrey,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ver Menú y Detalles",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ShadowGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
