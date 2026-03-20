package com.ariaai.companion.feature.homecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ariaai.companion.core.ui.AppColors
import com.ariaai.companion.core.ui.Spacing
import com.ariaai.companion.feature.homecontrol.component.AliasEditor
import com.ariaai.companion.feature.homecontrol.component.AuditLogRow

const val HOME_CONTROL_ROUTE = "homecontrol"

@Composable
fun HomeControlScreen(viewModel: HomeControlViewModel = hiltViewModel()) {
    val auditLog by viewModel.auditLog.collectAsStateWithLifecycle()
    val aliases by viewModel.aliases.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.md),
    ) {
        // Section 1: Recent Commands (Audit Log)
        item {
            Text(
                "Recent Commands",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.OnSurface,
            )
            Spacer(Modifier.height(Spacing.sm))
        }

        if (auditLog.isEmpty()) {
            item {
                Text(
                    "No commands yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.OnSurface,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Voice commands to your devices will appear here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.OnSurfaceVariant,
                )
            }
        } else {
            items(auditLog, key = { it.id }) { entry ->
                AuditLogRow(entry = entry)
                Spacer(Modifier.height(Spacing.sm))
            }
        }

        // Section 2: Device Aliases
        item {
            Spacer(Modifier.height(Spacing.xl))
            Text(
                "Device Aliases",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.OnSurface,
            )
            Spacer(Modifier.height(Spacing.sm))
        }

        item {
            AliasEditor(
                aliases = aliases,
                onAdd = viewModel::addAlias,
                onDelete = viewModel::deleteAlias,
            )
        }
    }
}
