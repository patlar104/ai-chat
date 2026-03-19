package com.example.aicompanion.feature.voice.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aicompanion.core.domain.model.VoiceState

private val MicButtonSize = 56.dp
private val WaveBarWidth = 4.dp
private val WaveBarSpacing = 4.dp

@Composable
fun AnimatedMicButton(
    voiceState: VoiceState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (voiceState) {
            is VoiceState.Idle -> MaterialTheme.colorScheme.primary
            is VoiceState.Listening -> MaterialTheme.colorScheme.primary
            is VoiceState.Transcribing -> MaterialTheme.colorScheme.surfaceVariant
            is VoiceState.Processing -> MaterialTheme.colorScheme.surfaceVariant
            is VoiceState.Speaking -> MaterialTheme.colorScheme.primary
            is VoiceState.Error -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(durationMillis = 200),
        label = "mic_button_color",
    )

    val contentDesc = when (voiceState) {
        is VoiceState.Idle -> "Start listening"
        is VoiceState.Listening -> "Stop listening"
        is VoiceState.Transcribing -> "Processing"
        is VoiceState.Processing -> "Processing"
        is VoiceState.Speaking -> "Speaking — tap to stop"
        is VoiceState.Error -> "Error"
    }

    Box(
        modifier = modifier
            .size(MicButtonSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onTap)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center,
    ) {
        when (voiceState) {
            is VoiceState.Listening -> {
                ListeningPulseContent()
            }
            is VoiceState.Transcribing, is VoiceState.Processing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 2.dp,
                )
            }
            is VoiceState.Speaking -> {
                SpeakingWaveContent()
            }
            else -> {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ListeningPulseContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "listening_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    Box(
        modifier = Modifier
            .size(MicButtonSize)
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SpeakingWaveContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_wave")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar1_height",
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar2_height",
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar3_height",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(WaveBarWidth)
                .height(bar1Height.dp)
                .background(Color.White, CircleShape),
        )
        Spacer(modifier = Modifier.width(WaveBarSpacing))
        Box(
            modifier = Modifier
                .width(WaveBarWidth)
                .height(bar2Height.dp)
                .background(Color.White, CircleShape),
        )
        Spacer(modifier = Modifier.width(WaveBarSpacing))
        Box(
            modifier = Modifier
                .width(WaveBarWidth)
                .height(bar3Height.dp)
                .background(Color.White, CircleShape),
        )
    }
}
