package com.uniandesfood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandesfood.data.model.HourlyWaitProfile
import com.uniandesfood.data.model.HourlyWaitSlot
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.PeakHoursViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestTimeToGoScreen(
    restaurant: Restaurant?,
    onBack: () -> Unit,
    peakHoursViewModel: PeakHoursViewModel = viewModel()
) {
    val uiState by peakHoursViewModel.uiState.collectAsState()
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    LaunchedEffect(restaurant?.id) {
        restaurant?.id?.let { peakHoursViewModel.loadHourlyProfile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Best time to go",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ShadowGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (restaurant != null) {
                            Text(
                                text = restaurant.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShadowGrey
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { peakHoursViewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh wait times",
                            tint = ShadowGrey
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurfaceWhite)
            )
        },
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        val profile = uiState.profile

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Eventual connectivity status banner
            if (uiState.isOffline) {
                item(key = "offline_notice") {
                    OfflineWaitNoticeCard(
                        message = if (uiState.hasNeverLoadedOffline) {
                            "Offline · connect once to load the wait time reports"
                        } else {
                            "Offline · the wait times may be outdated"
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = UniandesAmber)
                            Text(
                                text = "Analyzing hourly wait reports...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (profile == null || profile.slots.isEmpty()) {
                item(key = "empty_state") {
                    EmptyWaitReportsCard(
                        isOfflineNeverLoaded = uiState.hasNeverLoadedOffline,
                        restaurantName = restaurant?.name ?: "this restaurant"
                    )
                }
            } else {
                // Hero Recommendation Card
                item(key = "recommendation_card") {
                    RecommendationHeroCard(
                        profile = profile,
                        fromCache = uiState.fromCache
                    )
                }

                // Section Title
                item(key = "hourly_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reported wait time by hour",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ShadowGrey
                        )
                        Text(
                            text = "${profile.totalReports} ${if (profile.totalReports == 1) "report" else "reports"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Hourly Breakdown Rows (stable keys for Compose recomposition efficiency)
                items(items = profile.slots, key = { it.hour }) { slot ->
                    HourlyWaitSlotCard(
                        slot = slot,
                        isCurrentHour = slot.hour == currentHour,
                        isRecommended = slot.hour == profile.recommendedHour
                    )
                }

                // Legend Footer
                item(key = "legend_card") {
                    WaitTimeLegendCard()
                }
            }
        }
    }
}

@Composable
private fun OfflineWaitNoticeCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ShadowGrey.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                tint = ShadowGrey,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun RecommendationHeroCard(
    profile: HourlyWaitProfile,
    fromCache: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = BorderStroke(1.5.dp, UniandesAmber),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = UniandesAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Quietest upcoming hour",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = ShadowGrey
                    )
                }

                Surface(
                    color = if (fromCache) UniandesAmber.copy(alpha = 0.14f) else StatusFastGreen.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (fromCache) "Cached" else "Live",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (fromCache) StatusModerateAmber else StatusFastGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = profile.recommendedHourLabel ?: "--:--",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = ShadowGrey
                )
                profile.recommendedWaitLabel?.let { label ->
                    Surface(
                        color = StatusFastGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusFastGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recommended based on student wait-time reports from now onward.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun HourlyWaitSlotCard(
    slot: HourlyWaitSlot,
    isCurrentHour: Boolean,
    isRecommended: Boolean
) {
    val statusColor = when (slot.waitCategory) {
        WaitTimeCategory.FAST -> StatusFastGreen
        WaitTimeCategory.MODERATE -> StatusModerateAmber
        WaitTimeCategory.LONG -> StatusLongRed
    }
    val progress = (slot.averageWaitMinutes / 20.0).toFloat().coerceIn(0.12f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = if (isRecommended) BorderStroke(1.5.dp, StatusFastGreen) else BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = slot.hourLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ShadowGrey
                    )
                    if (isCurrentHour) {
                        Surface(
                            color = UniandesAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ShadowGrey,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isRecommended) {
                        Surface(
                            color = StatusFastGreen.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "BEST",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusFastGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${slot.waitLabel} · ${slot.reportCount} ${if (slot.reportCount == 1) "report" else "reports"}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(BorderLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(statusColor)
                )
            }
        }
    }
}

@Composable
private fun EmptyWaitReportsCard(
    isOfflineNeverLoaded: Boolean,
    restaurantName: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = UniandesAmber,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (isOfflineNeverLoaded) {
                    "No saved wait reports offline"
                } else {
                    "No wait time reports yet"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ShadowGrey,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isOfflineNeverLoaded) {
                    "Connect to the internet once to download hourly wait patterns for $restaurantName."
                } else {
                    "Students have not submitted wait-time reports for $restaurantName yet. Check back soon or scan the QR code after visiting."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WaitTimeLegendCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = StatusFastGreen, label = "< 5-7 min")
            LegendItem(color = StatusModerateAmber, label = "8-15 min")
            LegendItem(color = StatusLongRed, label = "> 15 min")
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
