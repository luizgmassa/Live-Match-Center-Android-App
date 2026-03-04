package com.massa.livecenter.presentation.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.massa.livecenter.domain.model.Commentary
import com.massa.livecenter.domain.model.CommentaryType

/**
 * A single row in the commentary feed showing the [commentary]'s minute, an event-type icon,
 * and the event description text.
 */
@Composable
fun CommentaryItem(
    commentary: Commentary,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (commentary.type) {
        CommentaryType.GOAL -> Icons.Default.SportsSoccer to Color(0xFF43A047)
        CommentaryType.CARD -> Icons.Default.CreditCard to Color(0xFFFFA726)
        CommentaryType.SUBSTITUTION -> Icons.Default.ChangeCircle to Color(0xFF1E88E5)
        CommentaryType.GENERAL -> Icons.Outlined.Circle to MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${commentary.minute}'",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = commentary.type.name,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = commentary.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
