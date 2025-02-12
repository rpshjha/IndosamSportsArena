package com.indosam.sportsarena.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.indosam.sportsarena.screens.AuctionFlowScreen
import com.indosam.sportsarena.screens.AuctionScreen
import com.indosam.sportsarena.screens.HomeScreen
import com.indosam.sportsarena.screens.ScheduleScreen
import com.indosam.sportsarena.screens.SplashScreen
import com.indosam.sportsarena.screens.TeamsScreen
import com.indosam.sportsarena.ui.theme.IndosamSportsArenaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndosamSportsArenaTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen { navController.navigate("home") } }
                    composable("home") { HomeScreen(navController) }
                    composable("auction") { AuctionScreen(navController) }
                    composable("auctionFlow/{selectedTeamsJson}") { backStackEntry ->
                        val selectedTeamsJson = backStackEntry.arguments?.getString("selectedTeamsJson") ?: ""
                        AuctionFlowScreen(
                            navController = navController,
                            selectedTeamsJson = selectedTeamsJson
                        )
                    }
                    composable("teams") { TeamsScreen(navController) }
                    composable("schedule") { ScheduleScreen(navController) }
                }
            }
        }
    }
}