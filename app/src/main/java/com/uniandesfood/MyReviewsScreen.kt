package com.uniandesfood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.local.MyReview
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.MyReviewsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyReviewsScreen(
    onBack: () -> Unit,
    viewModel: MyReviewsViewModel = viewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOnline by ConnectivityHelper.isOnlineFlow.collectAsState()

    val pendingCount = remember(reviews) { reviews.count { !it.isSent } }
    val average = remember(reviews) {
        if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("< Back", color = TextMuted) }

        Text(
            text = "My Reviews",
            style = MaterialTheme.typography.headlineLarge,
            color = ShadowGrey
        )
        Text(
            text = "Reviews you wrote with the QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MyReviewsSummaryCard("Total", "${reviews.size}", Modifier.weight(1f))
            MyReviewsSummaryCard("Average", String.format(Locale.US, "%.1f", average), Modifier.weight(1f))
            MyReviewsSummaryCard("Pending", "$pendingCount", Modifier.weight(1f))
        }

        if (!isOnline && pendingCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You are offline. Pending reviews will be sent when the internet comes back.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = UniandesAmber)
            }
        } else if (reviews.isEmpty()) {
            Text(
                text = "You have not written reviews yet. Scan the QR code in a restaurant to rate it.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(reviews, key = { it.id }) { review ->
                    MyReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun MyReviewsSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ShadowGrey
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun MyReviewCard(review: MyReview) {
    val date = remember(review.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(review.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.restaurantName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ShadowGrey,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (review.isSent) StatusFastGreen.copy(alpha = 0.15f) else StatusModerateAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (review.isSent) "Sent" else "Waiting for internet",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ShadowGrey,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rating: ${review.rating}/5",
                style = MaterialTheme.typography.bodyMedium,
                color = ShadowGrey
            )
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}