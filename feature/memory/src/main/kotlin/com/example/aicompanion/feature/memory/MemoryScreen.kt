package com.example.aicompanion.feature.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aicompanion.core.domain.model.MemoryEntry
import com.example.aicompanion.core.ui.Spacing

const val MEMORY_ROUTE = "memory"

@Composable
fun MemoryScreen(viewModel: MemoryViewModel = hiltViewModel()) {
    val memoryItems by viewModel.memoryItems.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "Memory (Phase 4)",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "Capture important notes so the assistant can recall them later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = detail,
            onValueChange = { detail = it },
            label = { Text("Detail") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = {
                viewModel.addMemory(title.trim(), detail.trim())
                title = ""
                detail = ""
            }) {
                Icon(imageVector = Icons.Filled.Save, contentDescription = "Save memory")
                Spacer(Modifier.height(0.dp))
                Text("Save Memory")
            }
        }

        Divider()

        if (memoryItems.isEmpty()) {
            Text(
                text = "No saved memories yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(memoryItems, key = { it.id }) { memory ->
                    MemoryRow(memory = memory, onDelete = { viewModel.deleteMemory(memory.id) })
                }
            }
        }
    }
}

@Composable
private fun MemoryRow(memory: MemoryEntry, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.md)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(memory.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(memory.detail, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Saved: ${java.text.SimpleDateFormat("EEE, MMM d h:mm a", java.util.Locale.getDefault()).format(java.util.Date(memory.createdAtMs))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete memory")
            }
        }
    }
}
