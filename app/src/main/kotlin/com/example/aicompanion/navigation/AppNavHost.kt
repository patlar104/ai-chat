package com.example.aicompanion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aicompanion.feature.avatar.AVATAR_ROUTE
import com.example.aicompanion.feature.avatar.AvatarScreen
import com.example.aicompanion.feature.chat.CHAT_ROUTE
import com.example.aicompanion.feature.chat.ChatScreen
import com.example.aicompanion.feature.homecontrol.HOME_CONTROL_ROUTE
import com.example.aicompanion.feature.homecontrol.HomeControlScreen
import com.example.aicompanion.feature.memory.MEMORY_ROUTE
import com.example.aicompanion.feature.memory.MemoryScreen
import com.example.aicompanion.feature.settings.SETTINGS_ROUTE
import com.example.aicompanion.feature.settings.SettingsScreen
import com.example.aicompanion.feature.tasks.TASKS_ROUTE
import com.example.aicompanion.feature.tasks.TasksScreen
import com.example.aicompanion.feature.voice.VOICE_ROUTE
import com.example.aicompanion.feature.voice.VoiceScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = VOICE_ROUTE,
        modifier = modifier,
    ) {
        composable(VOICE_ROUTE) { VoiceScreen() }
        composable(CHAT_ROUTE) { ChatScreen() }
        composable(AVATAR_ROUTE) { AvatarScreen() }
        composable(MEMORY_ROUTE) { MemoryScreen() }
        composable(HOME_CONTROL_ROUTE) { HomeControlScreen() }
        composable(TASKS_ROUTE) { TasksScreen() }
        composable(SETTINGS_ROUTE) { SettingsScreen() }
    }
}
