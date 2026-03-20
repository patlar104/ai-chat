@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ariaai.companion.feature.homecontrol.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.ariaai.companion.core.domain.model.Alias
import com.ariaai.companion.core.ui.AppColors
import com.ariaai.companion.core.ui.Spacing

private val DOMAIN_OPTIONS = listOf("light", "switch", "script", "scene", "climate")

@Composable
fun AliasEditor(
    aliases: List<Alias>,
    onAdd: (alias: String, entityId: String, domain: String) -> Unit,
    onDelete: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var aliasToDelete by remember { mutableStateOf<Alias?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Existing aliases list
        aliases.forEach { alias ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alias.alias,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.OnSurface,
                    )
                    Text(
                        text = alias.entityId,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.OnSurfaceVariant,
                    )
                }
                IconButton(onClick = { aliasToDelete = alias }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete alias ${alias.alias}",
                        tint = AppColors.Error,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        if (showAddForm) {
            AddAliasForm(
                onSave = { aliasName, entityId, domain ->
                    onAdd(aliasName, entityId, domain)
                    showAddForm = false
                },
                onCancel = { showAddForm = false },
            )
        } else {
            // "Add Alias" button
            Button(
                onClick = { showAddForm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = AppColors.OnPrimary,
                ),
            ) {
                Text("Add Alias")
            }
        }
    }

    // Delete confirmation dialog
    aliasToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { aliasToDelete = null },
            title = { Text("Delete this alias?") },
            text = { Text("Commands using this name will stop working.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target.id)
                        aliasToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { aliasToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AddAliasForm(
    onSave: (alias: String, entityId: String, domain: String) -> Unit,
    onCancel: () -> Unit,
) {
    var aliasName by remember { mutableStateOf("") }
    var entityId by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf(DOMAIN_OPTIONS.first()) }
    var domainMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = aliasName,
            onValueChange = { aliasName = it },
            label = { Text("Alias Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(Spacing.sm))
        OutlinedTextField(
            value = entityId,
            onValueChange = { entityId = it },
            label = { Text("Entity ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(Spacing.sm))
        ExposedDropdownMenuBox(
            expanded = domainMenuExpanded,
            onExpandedChange = { domainMenuExpanded = !domainMenuExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = selectedDomain,
                onValueChange = {},
                readOnly = true,
                label = { Text("Domain") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = domainMenuExpanded,
                onDismissRequest = { domainMenuExpanded = false },
            ) {
                DOMAIN_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedDomain = option
                            domainMenuExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        Row {
            Button(
                onClick = {
                    if (aliasName.isNotBlank() && entityId.isNotBlank()) {
                        onSave(aliasName.trim(), entityId.trim(), selectedDomain)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = AppColors.OnPrimary,
                ),
            ) {
                Text("Save")
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
