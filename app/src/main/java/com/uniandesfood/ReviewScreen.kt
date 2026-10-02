package com.uniandesfood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uniandesfood.ui.theme.*

private const val MAX_COMMENT_LENGTH = 280

@Composable
fun ReviewScreen(
    restaurantName: String,
    onSubmit: (Int, String) -> Unit,
    onCancel: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rate your visit",
            style = MaterialTheme.typography.headlineMedium,
            color = ShadowGrey
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = restaurantName,
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "$star stars",
                    tint = if (star <= rating) UniandesAmber else Color.LightGray,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { rating = star }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= MAX_COMMENT_LENGTH) comment = it },
            label = { Text("Comment (optional)") },
            supportingText = { Text("${comment.length}/$MAX_COMMENT_LENGTH") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSubmit(rating, comment) },
            enabled = rating > 0,
            colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Submit review", color = ShadowGrey)
        }
        TextButton(onClick = onCancel) {
            Text(text = "Cancel", color = TextMuted)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    UniandesFoodTheme {
        ReviewScreen(restaurantName = "One Burrito - ML", onSubmit = { _, _ -> }, onCancel = {})
    }
}