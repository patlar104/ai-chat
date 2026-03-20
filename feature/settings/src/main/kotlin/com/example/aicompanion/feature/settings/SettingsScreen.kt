package com.example.aicompanion.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aicompanion.core.ui.Spacing

const val SETTINGS_ROUTE = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val haUrl by viewModel.haServerUrl.collectAsStateWithLifecycle()
    val tokenPresent by viewModel.haAccessTokenPresent.collectAsStateWithLifecycle()
    val ttsVoice by viewModel.ttsVoiceName.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()
    val backgroundAutomation by viewModel.backgroundAutomationEnabled.collectAsStateWithLifecycle()

    var urlInput by remember(haUrl) { mutableStateOf(haUrl) }
    var tokenInput by remember { mutableStateOf("") }
    var tokenVisible by remember { mutableStateOf(false) }
    var voiceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.md),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = Spacing.md),
        )

        // Section: Home Assistant
        Text(
            "Home Assistant",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.sm))

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Server URL") },
            placeholder = { Text("http://192.168.1.100:8123") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.sm))

        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            label = { Text("Access Token") },
            placeholder = { Text(if (tokenPresent) "Token saved (enter new to replace)" else "Long-lived access token") },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                    Icon(
                        if (tokenVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (tokenVisible) "Hide token" else "Show token",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Section: Voice
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Voice",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))

        ExposedDropdownMenuBox(
            expanded = voiceExpanded,
            onExpandedChange = { voiceExpanded = it },
        ) {
            OutlinedTextField(
                value = ttsVoice ?: "System default",
                onValueChange = {},
                readOnly = true,
                label = { Text("TTS Voice") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )
            ExposedDropdownMenu(
                expanded = voiceExpanded,
                onDismissRequest = { voiceExpanded = false },
            ) {
                viewModel.availableVoices.forEach { voice ->
                    DropdownMenuItem(
                        text = { Text(voice) },
                        onClick = {
                            viewModel.saveTtsVoice(voice)
                            voiceExpanded = false
                        },
                    )
                }
            }
        }

        // Section: Privacy
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Privacy",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Block cloud AI and cloud TTS",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = privacyMode, onCheckedChange = { viewModel.setPrivacyMode(it) })
        }

        // Section: Automation
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Automation",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Enable morning briefings and scheduled automations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = backgroundAutomation, onCheckedChange = { viewModel.setBackgroundAutomation(it) })
        }

        // Save button
        Spacer(Modifier.height(Spacing.xl))
        Button(
            onClick = {
                viewModel.saveHaServerUrl(urlInput)
                if (tokenInput.isNotBlank()) {
                    viewModel.saveHaAccessToken(tokenInput)
                    tokenInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xl),
        ) {
            Text("Save Settings")
        }
    }
}
