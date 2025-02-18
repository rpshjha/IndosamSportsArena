package com.indosam.sportsarena.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.components.CustomButton
import com.indosam.sportsarena.models.Player
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
            // You can use a Toast or a Snackbar depending on your UI framework
            // For example, if using Snackbar:
            // scaffoldState.snackbarHostState.showSnackbar(it)
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
                        tint = Color.Blue
                    )
                }
            }

            // Table Layout
            TeamTable(
                selectedTeamInfo,
                auctionState.value.teamPlayers,
                auctionState.value.teamBudgets
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
        }
    }
}

@Composable
fun TeamTable(
    selectedTeamInfo: Map<String, Pair<String, String>>,
    teamPlayers: Map<String, List<Player>>,
    teamBudgets: Map<String, Int>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(1.dp))
    ) {
        Column(modifier = Modifier.padding(1.dp)) {
            val attributes =
                listOf("Team Name", "Captain", "VC", "P1", "P2", "P3", "P4", "P5", "P6")
            attributes.forEach { attribute ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = attribute,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Gray)
                            .padding(1.dp)
                    )
                    selectedTeamInfo.keys.forEach { team ->
                        val data = when (attribute) {
                            "Team Name" -> team
                            "Captain" -> getFirstName(selectedTeamInfo[team]?.first ?: "TBD")
                            "VC" -> getFirstName(selectedTeamInfo[team]?.second ?: "TBD")
                            in listOf("P1", "P2", "P3", "P4", "P5", "P6") -> {
                                val playerIndex = attribute.substring(1).toInt() - 1
                                teamPlayers[team]?.getOrNull(playerIndex)
                                    ?.let { getFirstName(it.name) } ?: "TBD"
                            }

                            else -> "N/A"
                        }
                        Text(
                            text = data,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Gray)
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getFirstName(fullName: String): String {
    return fullName.split(" ").firstOrNull() ?: fullName
}

@Composable
fun PointsTable(teamBudgets: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(1.dp))
    ) {
        Column(modifier = Modifier.padding(1.dp)) {
            val attributes = listOf("Points Utilised", "Points Left", "Total Budget")
            attributes.forEach { attribute ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = attribute,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Gray)
                            .padding(1.dp)
                    )
                    teamBudgets.keys.forEach { team ->
                        val data = when (attribute) {
                            "Points Utilised" -> (1000 - (teamBudgets[team] ?: 1000)).toString()
                            "Points Left" -> (teamBudgets[team] ?: 1000).toString()
                            "Total Budget" -> "1000"
                            else -> "N/A"
                        }
                        Text(
                            text = data,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Gray)
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuctionControls(
    viewModel: AuctionViewModel,
    currentBidder: String,
    remainingPlayers: List<Player>,
    currentBid: Int,
    onBid: (Int) -> Unit,
    onSkip: () -> Unit,
    onAssign: () -> Unit,
    onAssignUnsold: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val currentPlayer = remainingPlayers.firstOrNull()

        if (currentPlayer != null) {
            // Set the initial bid to the player's base point
            val initialBid = currentPlayer.basePoint
            val currentBidAmount = if (currentBid < initialBid) initialBid else currentBid

            Text(
                text = "Current Team Bidding: $currentBidder",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Current Player for Auction: ${getFirstName(currentPlayer.name)} (${currentPlayer.basePoint})",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Current Bidding Amount: $currentBidAmount",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(
                    text = "Place Bid",
                    onClick = { onBid(currentBidAmount + 50) }, // Increment by 50
                    enabled = remainingPlayers.isNotEmpty() && viewModel.canPlaceBid(
                        currentBidder,
                        currentBidAmount + 50
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                CustomButton(
                    text = "Skip Turn",
                    onClick = onSkip,
                    enabled = remainingPlayers.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = "Assign Player",
                onClick = onAssign,
                enabled = remainingPlayers.isNotEmpty(),
            )
        } else {
            Text(
                text = "No players remaining for auction!",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
        }

        CustomButton(
            text = "Assign Unsold Players",
            onClick = onAssignUnsold,
            enabled = remainingPlayers.isNotEmpty(),
        )
    }
}