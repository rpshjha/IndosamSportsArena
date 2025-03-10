package com.indosam.sportsarena.screens.auction

import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.indosam.sportsarena.components.CustomAlertDialog
import com.indosam.sportsarena.components.CustomButtonWithTooltip
import com.indosam.sportsarena.models.AuctionLog
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.screens.AuctionInfoButton
import com.indosam.sportsarena.screens.BaseScreen
import com.indosam.sportsarena.screens.TossDialog
import com.indosam.sportsarena.utils.JsonUtils.parseSelectedTeamInfo
import com.indosam.sportsarena.utils.ResourceUtils
import com.indosam.sportsarena.utils.StringUtils.getFirstName
import com.indosam.sportsarena.viewmodels.AuctionViewModel
import kotlinx.coroutines.launch

@Composable
fun AuctionFlowScreen(
    navController: NavController,
    selectedTeamsJson: String,
    viewModel: AuctionViewModel = viewModel()
) {
    LocalContext.current

    val selectedTeamInfo = parseSelectedTeamInfo(selectedTeamsJson)
    val excludeNames = selectedTeamInfo.values.flatMap { listOf(it.first, it.second) }

    val playersState = viewModel.players.collectAsState()
    val auctionState = viewModel.auctionState.collectAsState()
    val auctionLogs = viewModel.auctionLogs.collectAsState()
    val toastMessage = viewModel.toastMessage.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()

    val showTossDialog = viewModel.showTossDialog.collectAsState().value
    val tossWinner = viewModel.tossWinner.collectAsState().value

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
                    onBid = { viewModel.handleBid() },
                    onSkip = { viewModel.skipTurn() },
                    onAssignCurrent = { viewModel.assignCurrentPlayer() },
                    onNext = { viewModel.nextTeam() },
                    onAssignRemaining = { viewModel.assignRemainingPlayers() },
                    onAssignUnsold = { viewModel.assignUnsoldPlayers() },
                    viewModel = viewModel
                )

                if (showTossDialog) {
                    TossDialog(
                        showDialog = remember { viewModel.showTossDialog },
                        onConfirm = { viewModel.assignPlayerAfterToss() },
                        onDismiss = { viewModel.dismissTossDialog() },
                        tossWinner = tossWinner
                    )
                }
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
    onBid: () -> Unit,
    onSkip: () -> Unit,
    onAssignCurrent: () -> Unit,
    onNext: () -> Unit,
    onAssignRemaining: () -> Unit,
    onAssignUnsold: () -> Unit,
    viewModel: AuctionViewModel
) {
    val teamDecisions = viewModel.teamDecisions.collectAsState().value

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
                    onAssignCurrent = onAssignCurrent,
                    onNext = onNext,
                    onAssignRemaining = onAssignRemaining,
                    onAssignUnsold = onAssignUnsold,
                    teamDecisions = teamDecisions,
                    viewModel = viewModel
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
            } else {
                players.forEach { player ->
                    Text(
                        text = "${getFirstName(player.name)} (${player.basePoint})",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
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
    onBid: () -> Unit,
    onSkip: () -> Unit,
    onAssignCurrent: () -> Unit,
    onNext: () -> Unit,
    onAssignRemaining: () -> Unit,
    onAssignUnsold: () -> Unit,
    teamDecisions: Map<String, String>,
    viewModel: AuctionViewModel
) {
    val showAssignCurrentDialog = remember { mutableStateOf(false) }
    val showAssignRemainingDialog = remember { mutableStateOf(false) }
    val allTeamsMadeDecision = teamDecisions.size == auctionState.teams.size
    val isFirstBidderInRound = auctionState.currentBid == 0


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val currentPlayer = remainingPlayers.firstOrNull()

        if (currentPlayer != null) {
            val initialBid = currentPlayer.basePoint
            val currentBidAmount = if (currentBid < initialBid) initialBid else currentBid

            val hasPlacedBid = teamDecisions[currentBidder] == "BID"
            val hasSkipped = teamDecisions[currentBidder] == "SKIP"

            val canAffordBid = viewModel.canPlaceBid(currentBidder, currentBidAmount + 50)
            val isPlaceBidEnabled = !hasSkipped && canAffordBid && remainingPlayers.isNotEmpty()
            val isSkipTurnEnabled = !hasPlacedBid && !hasSkipped && remainingPlayers.isNotEmpty()
            val isMoveToNextEnabled = !isPlaceBidEnabled && !isSkipTurnEnabled
            val maxBid = viewModel.calculateMaxBid(currentBidder)

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
            if (currentBid != 0) {
                Text(
                    text = "Last Bid Amount: $currentBidAmount",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 16.dp)
                )
            }
            Text(
                text = if (isFirstBidderInRound) {
                    "Min Bid Amount Required: $initialBid"
                } else {
                    val nextBid = currentBidAmount + 50
                    if (nextBid <= 350) {
                        "Min Bid Amount Required: $nextBid"
                    } else {
                        "Bid limit reached! Cannot exceed 350."
                    }
                },
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Max Bid $currentBidder Can Place is $maxBid",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButtonWithTooltip(
                    text = "Place Bid",
                    onClick = { onBid() },
                    enabled = isPlaceBidEnabled,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.Green,
                    tooltipText = if (!canAffordBid) "Cannot place bid: Max budget reached." else null
                )
                Spacer(modifier = Modifier.width(8.dp))
                CustomButtonWithTooltip(
                    text = "Skip Turn",
                    onClick = { onSkip() },
                    enabled = isSkipTurnEnabled,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.Red,
                    tooltipText = if (!canAffordBid) "Cannot place bid: Max budget reached." else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButtonWithTooltip(
                    text = "Assign Current Player",
                    onClick = {
                        showAssignCurrentDialog.value = true
                    },
                    enabled = allTeamsMadeDecision,
                    modifier = Modifier.weight(1f),
                    tooltipText = if (!allTeamsMadeDecision) "Cannot assign player: No decision (bid/skip) made by the team." else null
                )

                if (showAssignCurrentDialog.value) {
                    CustomAlertDialog(
                        showDialog = showAssignCurrentDialog,
                        title = "Confirm Action",
                        text = "Are you sure you want to assign the current player?",
                        onConfirm = {
                            onAssignCurrent()
                        },
                        onDismiss = { Log.d("CustomAlertDialog", "Dialog dismissed") }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                CustomButtonWithTooltip(
                    text = "Move to Next Team",
                    onClick = onNext,
                    enabled = isMoveToNextEnabled,
                    modifier = Modifier.weight(1f),
                    tooltipText = if (!allTeamsMadeDecision) "Cannot move to next team: Current team has not made a decision (bid/skip)." else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButtonWithTooltip(
                    text = "Assign Remaining",
                    onClick = {
                        showAssignRemainingDialog.value = true
                    },
                    enabled = auctionState.remainingPlayers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    tooltipText = if (auctionState.remainingPlayers.isEmpty()) "No Remaining players available to assign." else null
                )

                if (showAssignRemainingDialog.value) {
                    CustomAlertDialog(
                        showDialog = showAssignRemainingDialog,
                        title = "Confirm Action",
                        text = "Are you sure you want to assign the remaining players?",
                        onConfirm = {
                            onAssignRemaining()
                        },
                        onDismiss = { Log.d("CustomAlertDialog", "Dialog dismissed") }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                CustomButtonWithTooltip(
                    text = "Assign Unsold",
                    onClick = onAssignUnsold,
                    enabled = auctionState.unsoldPlayers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    tooltipText = if (auctionState.unsoldPlayers.isEmpty()) "No unsold players available to assign." else null
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
    val context = LocalContext.current
    val maxBudget = ResourceUtils.getMaxBudget(context)

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
                            "Points Utilised" -> (maxBudget - (teamBudgets[team]
                                ?: maxBudget)).toString()

                            "Points Left" -> (teamBudgets[team] ?: maxBudget).toString()
                            "Total Budget" -> maxBudget.toString()
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
                logs.reversed().forEach { log ->
                    Text(
                        text = log.message,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                }
            }
        }
    }
}
