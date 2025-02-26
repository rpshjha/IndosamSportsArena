package com.indosam.sportsarena.screens.auction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.indosam.sportsarena.components.CustomButton
import com.indosam.sportsarena.models.AuctionLog
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.screens.AuctionInfoButton
import com.indosam.sportsarena.screens.BaseScreen
import com.indosam.sportsarena.utils.JsonUtils.parseSelectedTeamInfo
import com.indosam.sportsarena.utils.StringUtils.getFirstName
import com.indosam.sportsarena.viewmodels.AuctionViewModel
import kotlinx.coroutines.launch

@Composable
fun AuctionFlowScreen(
    navController: NavController,
    selectedTeamsJson: String,
    viewModel: AuctionViewModel = viewModel()
) {
    val context = LocalContext.current

    val selectedTeamInfo = parseSelectedTeamInfo(selectedTeamsJson)
    val excludeNames = selectedTeamInfo.values.flatMap { listOf(it.first, it.second) }

    val playersState = viewModel.players.collectAsState()
    val auctionState = viewModel.auctionState.collectAsState()
    val auctionLogs = viewModel.auctionLogs.collectAsState()
    val toastMessage = viewModel.toastMessage.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadPlayers(excludeNames)
    }

    LaunchedEffect(playersState.value) {
        viewModel.updateRemainingPlayers(playersState.value)
    }

    LaunchedEffect(toastMessage.value) {
        toastMessage.value?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearToastMessage()
            }
        }
    }

    BaseScreen(
        title = "Auction Flow",
        navController = navController,
        showBackButton = true,
        showHomeButton = true
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            content = { paddingValues ->
                AuctionContent(
                    modifier = Modifier
                        .fillMaxSize()

                        .padding(paddingValues)
                        .padding(16.dp),
                    navController = navController,
                    selectedTeamInfo = selectedTeamInfo,
                    auctionState = auctionState.value,
                    auctionLogs = auctionLogs.value,
                    onBid = { bid -> viewModel.handleBid(bid) },
                    onSkip = { viewModel.skipTurn() },
                    onNext = { viewModel.nextTeam() },
                    onAssign = { viewModel.assignPlayer() },
                    onAssignUnsold = { viewModel.assignUnsoldPlayers() }
                )
            }
        )
    }
}

@Composable
private fun AuctionContent(
    modifier: Modifier,
    navController: NavController,
    selectedTeamInfo: Map<String, Pair<String, String>>,
    auctionState: AuctionState,
    auctionLogs: List<AuctionLog>,
    onBid: (Int) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onAssign: () -> Unit,
    onAssignUnsold: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AuctionInfoButton(navController)
                    TeamTable(selectedTeamInfo, auctionState.teamPlayers)
                    Spacer(modifier = Modifier.height(16.dp))
                    PointsTable(auctionState.teamBudgets)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AuctionControls(
                    currentBidder = auctionState.currentBidder,
                    remainingPlayers = auctionState.remainingPlayers,
                    currentBid = auctionState.currentBid,
                    auctionState = auctionState,
                    onBid = onBid,
                    onSkip = onSkip,
                    onNext = onNext,
                    onAssign = onAssign,
                    onAssignUnsold = onAssignUnsold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                PlayersListSection(
                    remainingPlayers = auctionState.remainingPlayers,
                    unsoldPlayers = auctionState.unsoldPlayers
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AuctionLogsSection(auctionLogs)
            }
        }
    }
}

@Composable
private fun PlayersListSection(
    remainingPlayers: List<Player>,
    unsoldPlayers: List<Player>
) {
    AnimatedVisibility(
        visible = remainingPlayers.isNotEmpty() || unsoldPlayers.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            PlayerListColumn(
                title = "Remaining Players",
                players = remainingPlayers
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlayerListColumn(
                title = "Unsold Players",
                players = unsoldPlayers
            )

            if (remainingPlayers.isEmpty() && unsoldPlayers.isEmpty()) {
                Text(
                    text = "No players remaining or unsold.",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
private fun PlayerListColumn(title: String, players: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (players.isEmpty()) {
                Text(
                    text = "No players available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }else {
                players.forEach { player ->
                    Text(
                        text = "${getFirstName(player.name)} (${player.basePoint})",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun AuctionControls(
    currentBidder: String,
    remainingPlayers: List<Player>,
    currentBid: Int,
    auctionState: AuctionState,
    onBid: (Int) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onAssign: () -> Unit,
    onAssignUnsold: () -> Unit
) {
    val showConfirmationDialog = remember { mutableStateOf(false) }

    if (showConfirmationDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog.value = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to assign unsold players?") },
            confirmButton = {
                Button(onClick = {
                    onAssignUnsold()
                    showConfirmationDialog.value = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmationDialog.value = false }) {
                    Text("No")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val currentPlayer = remainingPlayers.firstOrNull()

        if (currentPlayer != null) {
            val initialBid = currentPlayer.basePoint
            val currentBidAmount = if (currentBid < initialBid) initialBid else currentBid

            Text(
                text = "Auction Round ${auctionState.currentRound}",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
                style = TextStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Text(
                text = "Bidding Team: $currentBidder",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Bidding Player: ${getFirstName(currentPlayer.name)} (${currentPlayer.basePoint})",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Bidding Amount: $currentBidAmount",
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
                    onClick = { onBid(currentBidAmount + 50) },
                    enabled = remainingPlayers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                CustomButton(
                    text = "Skip Turn",
                    onClick = onSkip,
                    enabled = remainingPlayers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(
                    text = "Next Team",
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(
                    text = "Assign Player",
                    onClick = onAssign,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CustomButton(
                    text = "Assign Unsold",
                    onClick = onAssignUnsold,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Text(
                text = "No players remaining for auction!",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun TeamTable(
    selectedTeamInfo: Map<String, Pair<String, String>>,
    teamPlayers: Map<String, List<Player>>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            val attributes =
                listOf("Team Name", "Captain", "VC", "P1", "P2", "P3", "P4", "P5", "P6")
            attributes.forEachIndexed { index, attribute ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = attribute,
                        fontSize = 16.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                    selectedTeamInfo.keys.forEachIndexed { _, team ->
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
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsTable(teamBudgets: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            val attributes = listOf("Points Utilised", "Points Left", "Total Budget")
            attributes.forEach { attribute ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = attribute,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
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
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }


}

@Composable
private fun AuctionLogsSection(logs: List<AuctionLog>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Auction Logs",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "No logs available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                logs.forEach { log ->
                    Text(
                        text = "${log.timestamp}: ${log.message}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}