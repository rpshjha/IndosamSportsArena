package com.indosam.sportsarena.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.indosam.sportsarena.screens.auction.AuctionFlowScreen
import com.indosam.sportsarena.screens.StartAuction
import com.indosam.sportsarena.screens.HomeScreen
import com.indosam.sportsarena.screens.KnowAuctionRules
import com.indosam.sportsarena.screens.BoxCricketFixtures
import com.indosam.sportsarena.screens.GalleryHighlightsScreen
import com.indosam.sportsarena.screens.KnowPlayers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("auction") { StartAuction(navController) }
                    composable("Know Auction Rules") { KnowAuctionRules(navController) }
                    composable("auctionFlow/{selectedTeamsJson}") { backStackEntry ->
                        val selectedTeamsJson = backStackEntry.arguments?.getString("selectedTeamsJson") ?: ""
                        AuctionFlowScreen(
                            navController = navController,
                            selectedTeamsJson = selectedTeamsJson
                        )
                    }
                    composable("teams") { KnowPlayers(navController) }
                    composable("schedule") {
                        val context = LocalContext.current
                        BoxCricketFixtures(navController = navController, context = context)
                    }
                    composable("gallery") { GalleryHighlightsScreen(navController) }
                }

        }
    }
}