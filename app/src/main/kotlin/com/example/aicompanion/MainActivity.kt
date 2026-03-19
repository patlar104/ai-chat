package com.example.aicompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.aicompanion.core.ui.AICompanionTheme
import com.example.aicompanion.navigation.AppNavHost
import com.example.aicompanion.navigation.BottomNavBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AICompanionTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavBar(navController = navController) },
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
