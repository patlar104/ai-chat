package com.example.aicompanion.feature.homecontrol.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.aicompanion.core.domain.model.AuditLogEntry
import com.example.aicompanion.core.domain.model.AuditStatus
import com.example.aicompanion.core.ui.AppColors
import com.example.aicompanion.core.ui.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
fun AuditLogRow(entry: AuditLogEntry, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.sm))
            .background(AppColors.SurfaceContainer)
            .padding(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.command,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.OnSurface,
            )
            val reason = entry.errorReason
            if (entry.status == AuditStatus.FAILURE && reason != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Error,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeFormatter.format(Date(entry.timestampMs)),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.OnSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))
            when (entry.status) {
                AuditStatus.SUCCESS -> Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = AppColors.AuditSuccess,
                )
                AuditStatus.FAILURE -> Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = "Failure",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
