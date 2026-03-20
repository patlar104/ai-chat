package com.ariaai.companion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ariaai.companion.feature.avatar.AVATAR_ROUTE
import com.ariaai.companion.feature.avatar.AvatarScreen
import com.ariaai.companion.feature.chat.CHAT_ROUTE
import com.ariaai.companion.feature.chat.ChatScreen
import com.ariaai.companion.feature.homecontrol.HOME_CONTROL_ROUTE
import com.ariaai.companion.feature.homecontrol.HomeControlScreen
import com.ariaai.companion.feature.memory.MEMORY_ROUTE
import com.ariaai.companion.feature.memory.MemoryScreen
import com.ariaai.companion.feature.settings.SETTINGS_ROUTE
import com.ariaai.companion.feature.settings.SettingsScreen
import com.ariaai.companion.feature.tasks.TASKS_ROUTE
import com.ariaai.companion.feature.tasks.TasksScreen
import com.ariaai.companion.feature.voice.VOICE_ROUTE
import com.ariaai.companion.feature.voice.VoiceScreen

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
