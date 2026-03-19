package com.example.aicompanion.feature.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.aicompanion.core.domain.model.Message
import com.example.aicompanion.core.domain.model.MessageRole
import com.example.aicompanion.core.domain.model.SourceType
import com.example.aicompanion.core.ui.AppColors
import com.example.aicompanion.core.ui.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val userBubbleShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 24.dp,
    bottomEnd = 4.dp,
)

private val assistantBubbleShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 4.dp,
    bottomEnd = 24.dp,
)

private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    val isUser = message.role == MessageRole.USER
    val horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.72f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            // Message bubble
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) AppColors.OnPrimary else AppColors.OnSurface,
                modifier = Modifier
                    .clip(if (isUser) userBubbleShape else assistantBubbleShape)
                    .background(if (isUser) AppColors.Primary else AppColors.SurfaceVariant)
                    .padding(Spacing.md),
            )

            // Source tier chip for assistant messages
            if (!isUser) {
                Spacer(Modifier.height(Spacing.xs))
                val chipLabel = when (message.sourceType) {
                    SourceType.DETERMINISTIC -> "deterministic"
                    SourceType.UNKNOWN -> "unknown"
                }
                val chipBorderColor = when (message.sourceType) {
                    SourceType.DETERMINISTIC -> AppColors.Primary
                    SourceType.UNKNOWN -> AppColors.OnSurfaceVariant
                }
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = chipLabel,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = AppColors.Background,
                        labelColor = chipBorderColor,
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = chipBorderColor,
                    ),
                )
            }

            // Timestamp
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = timeFormatter.format(Date(message.timestampMs)),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.OnSurfaceVariant,
            )
        }
    }
}
