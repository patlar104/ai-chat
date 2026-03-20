package com.ariaai.companion.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
)

val bottomNavDestinations = listOf(
    BottomNavDestination("voice", "Voice", Icons.Filled.Mic, Icons.Outlined.Mic, "Voice"),
    BottomNavDestination("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat, "Chat"),
    BottomNavDestination("homecontrol", "Home", Icons.Filled.Home, Icons.Outlined.Home, "Home Control"),
    BottomNavDestination("memory", "Memory", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks, "Memory"),
    BottomNavDestination("tasks", "Tasks", Icons.Filled.Alarm, Icons.Outlined.Alarm, "Tasks"),
    BottomNavDestination("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "Settings"),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val currentEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry.value?.destination?.route

    NavigationBar {
        bottomNavDestinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.contentDescription,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}
