package com.massa.livecenter.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.massa.livecenter.data.remote.websocket.WebSocketConnectionState

/**
 * A small circular badge reflecting the current WebSocket [connectionState].
 *
 * | State        | Color  | Label        |
 * |--------------|--------|--------------|
 * | Connected    | Green  | Connected    |
 * | Reconnecting | Amber  | Reconnecting |
 * | Disconnected | Red    | Disconnected |
 */
@Composable
fun StatusBadge(
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (connectionState) {
        WebSocketConnectionState.Connected -> "● Connected" to Color(0xFF43A047)
        WebSocketConnectionState.Reconnecting -> "● Reconnecting" to Color(0xFFFFA726)
        WebSocketConnectionState.Disconnected -> "● Disconnected" to Color(0xFFE53935)
    }

    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 500),
        label = "status_badge_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(animatedColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = animatedColor
        )
    }
}
