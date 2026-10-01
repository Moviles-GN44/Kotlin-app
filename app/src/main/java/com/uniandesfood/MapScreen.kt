package com.uniandesfood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // Map positions relative to campus viewport [0f..1f, 0f..1f]
    // Well distributed within visible canvas bounds
    val restaurantMapCoords = remember {
        mapOf(
            "one_burrito_ml" to Pair(0.24f, 0.28f),         // ML Building area
            "el_toro_rgd" to Pair(0.52f, 0.44f),            // RGD central plaza
            "one_burrito_rgd" to Pair(0.72f, 0.46f),        // RGD east
            "burger_play_rgd" to Pair(0.48f, 0.58f),        // RGD south terrace
            "la_cabra_sanduchera_rgd" to Pair(0.70f, 0.60f),// RGD patio
            "la_liebre_franco" to Pair(0.68f, 0.25f),       // Franco building
            "burger_play_sd" to Pair(0.25f, 0.68f)          // SD building
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
    ) {
        // 1. Campus Map Container
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val mapWidthDp = maxWidth
            val mapHeightDp = maxHeight

            // Canvas: Roads, Campus Greens, Walkways
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Campus Background Slate
                drawRect(
                    color = Color(0xFFF1F5F9),
                    size = size
                )

                // Central Campus Green Zone (Jardines del Bobo / Plazoleta)
                drawRoundRect(
                    color = Color(0xFFDCFCE7),
                    topLeft = Offset(w * 0.12f, h * 0.20f),
                    size = Size(w * 0.76f, h * 0.48f),
                    cornerRadius = CornerRadius(32f, 32f)
                )

                // Secondary Garden (Bosque Uniandes / C-W Zone)
                drawRoundRect(
                    color = Color(0xFFE8F5E9),
                    topLeft = Offset(w * 0.15f, h * 0.12f),
                    size = Size(w * 0.40f, h * 0.12f),
                    cornerRadius = CornerRadius(20f, 20f)
                )

                // Campus Walkways (Senderos peatonales)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.10f, h * 0.26f),
                    end = Offset(w * 0.88f, h * 0.55f),
                    strokeWidth = 14f
                )
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.28f, h * 0.15f),
                    end = Offset(w * 0.50f, h * 0.72f),
                    strokeWidth = 12f
                )
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.70f, h * 0.22f),
                    end = Offset(w * 0.45f, h * 0.65f),
                    strokeWidth = 10f
                )
            }

            // Campus Building Zones Overlay with visible labels
            val buildingLayout = listOf(
                Pair(Pair(0.08f, 0.22f), "Edificio ML\n(Ingeniería)"),
                Pair(Pair(0.42f, 0.38f), "Edificio RGD\n(Centro Deportivo)"),
                Pair(Pair(0.58f, 0.15f), "Edificio Franco\n(Administración)"),
                Pair(Pair(0.08f, 0.62f), "Edificio SD\n(Salones)")
            )

            buildingLayout.forEach { (coord, name) ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = mapWidthDp * coord.first,
                            y = mapHeightDp * coord.second
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_building),
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp
                            ),
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Visible Interactive Restaurant Pins
            restaurants.forEach { rest ->
                val coord = restaurantMapCoords[rest.id] ?: Pair(0.5f, 0.5f)
                val isSelected = rest.id == selectedRestaurant?.id
                val shortName = when (rest.id) {
                    "one_burrito_ml" -> "One Burrito (ML)"
                    "el_toro_rgd" -> "El Toro (RGD)"
                    "one_burrito_rgd" -> "One Burrito (RGD)"
                    "burger_play_rgd" -> "Burger Play (RGD)"
                    "la_cabra_sanduchera_rgd" -> "La Cabra"
                    "la_liebre_franco" -> "La Liebre"
                    "burger_play_sd" -> "Burger Play (SD)"
                    else -> rest.name
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = (mapWidthDp * coord.first) - 40.dp,
                            y = (mapHeightDp * coord.second) - 16.dp
                        )
                        .shadow(
                            elevation = if (isSelected) 8.dp else 3.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) UniandesAmber else CardSurfaceWhite)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) UniandesAmber else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel?.selectRestaurant(rest.id) }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = rest.name,
                            tint = if (isSelected) ShadowGrey else StatusLongRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) ShadowGrey else TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Top Header: Search Bar & Restaurant Quick Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Search Bar Surface
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceWhite,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                            text = "Buscar restaurante en el campus...",
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
                            .padding(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_explore),
                            contentDescription = "Filtros",
                            tint = UniandesAmber,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Selection Chips Row for all campus restaurants
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                restaurants.forEach { rest ->
                    val isSelected = rest.id == selectedRestaurant?.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) UniandesAmber else CardSurfaceWhite.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) UniandesAmber else BorderLight
                        ),
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                        modifier = Modifier.clickable { viewModel?.selectRestaurant(rest.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) ShadowGrey else UniandesAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = rest.name.replace(" - RGD", "").replace(" - ML", "").replace(" - Franco", "").replace(" - SD", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) ShadowGrey else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating Bottom Restaurant Card
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
