package com.massa.livecenter.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A chip that displays a single odds value and briefly flashes an amber highlight
 * whenever [value] changes — providing visual feedback for real-time odds updates.
 *
 * @param label  e.g. "1", "X", "2"
 * @param value  the current odds decimal value, e.g. 1.85
 */
@Composable
fun OddsChip(
    label: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    var highlight by remember { mutableStateOf(false) }

    // Flash amber whenever the value changes
    LaunchedEffect(value) {
        highlight = true
        kotlinx.coroutines.delay(600)
        highlight = false
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (highlight) Color(0xFFFFA726) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "odds_chip_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$label  ${"%.2f".format(value)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
