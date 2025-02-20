package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.screens.BaseScreen
import com.indosam.sportsarena.viewmodels.AuctionViewModel

@Composable
fun AuctionFlowScreen(
    navController: NavController,
    selectedTeamsJson: String,
    viewModel: AuctionViewModel = viewModel()
) {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Pair<String, String>>>() {}.type
    val selectedTeamInfo = gson.fromJson<Map<String, Pair<String, String>>>(selectedTeamsJson, type)

    // Extract captain and vice-captain names to exclude them from remaining players
    val excludeNames = selectedTeamInfo.values.flatMap { listOf(it.first, it.second) }

    // Observe players and auction state from ViewModel
    val playersState = viewModel.players.collectAsState()
    val auctionState = viewModel.auctionState.collectAsState()
    val unsoldPlayersState = viewModel.unsoldPlayers.collectAsState()
    val toastMessage = viewModel.toastMessage.collectAsState()


    // Load players when the screen is launched
    LaunchedEffect(Unit) {
        viewModel.loadPlayers(excludeNames)
    }

    // Update UI when playersState changes
    LaunchedEffect(playersState.value) {
        viewModel.updateRemainingPlayers(playersState.value)
    }

    // Show toast message if any
    LaunchedEffect(toastMessage.value) {
        toastMessage.value?.let {
            // Show toast message here
        }
    }


    BaseScreen(
        title = "Auction Flow",
        navController = navController,
        showBackButton = true,
        showHomeButton = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add the "Know Auction Rules" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { navController.navigate("Know Auction Rules") }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Know Auction Rules",
                        tint = Color(0xFFBB86FC)
                    )
                }
            }

            // Table Layout
            TeamTable(
                selectedTeamInfo,
                auctionState.value.teamPlayers,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Points Table
            PointsTable(auctionState.value.teamBudgets)

            Spacer(modifier = Modifier.height(20.dp))

            // Auction Controls
            AuctionControls(
                viewModel = viewModel,
                currentBidder = auctionState.value.currentBidder,
                remainingPlayers = auctionState.value.remainingPlayers,
                currentBid = auctionState.value.currentBid,
                onBid = { bid -> viewModel.handleBid(bid) },
                onSkip = { viewModel.skipTurn() },
                onAssign = { viewModel.assignPlayer() },
                onAssignUnsold = { viewModel.assignUnsoldPlayers() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        if (auctionState.value.remainingPlayers.isNotEmpty()) {
                            Text(
                                text = "Remaining Players",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(bottom = 8.dp),
                                style = TextStyle(
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            RemainingPlayersList(auctionState.value.remainingPlayers)
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        if (unsoldPlayersState.value.isNotEmpty()) {
                            Text(
                                text = "Unsold Players",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(bottom = 8.dp),
                                style = TextStyle(
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            UnsoldPlayersList(unsoldPlayersState.value)
                        }
                    }
                }
            }
        }
    }
}


fun getFirstName(fullName: String): String {
    return fullName.split(" ").firstOrNull() ?: fullName
}