package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.components.CustomAlertDialog
import com.indosam.sportsarena.components.CustomButtonWithTooltip
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.utils.StringUtils.getFirstName
import com.indosam.sportsarena.viewmodels.AuctionViewModel
import kotlinx.coroutines.time.delay
import java.time.Duration

@Composable
fun AuctionControls(
    currentBidder: String,
    remainingPlayers: List<Player>,
    currentBid: Int,
    auctionState: AuctionState,
    onBid: (Int) -> Unit,
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
    val showBidDialog = remember { mutableStateOf(false) }
    var countdownTime by remember { mutableIntStateOf(30) }

    val allTeamsMadeDecision = teamDecisions.size == auctionState.teams.size
    val isFirstBidderInRound = auctionState.currentBid == 0
    val currentPlayer = remainingPlayers.firstOrNull()

    LaunchedEffect(auctionState.currentBidder, auctionState.currentRound) {
        countdownTime = 30
        while (countdownTime > 0) {
            delay(Duration.ofSeconds(1))
            countdownTime--
        }
        if (countdownTime == 0) {
            onSkip()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (currentPlayer != null) {
            val initialBid = currentPlayer.basePoint
            val currentBidAmount = if (currentBid < initialBid) initialBid else currentBid
            val hasPlacedBid = teamDecisions[currentBidder] == "BID"
            val hasSkipped = teamDecisions[currentBidder] == "SKIP"
            val canAffordBid = viewModel.canPlaceBid(currentBidder, currentBidAmount + 50)
            val maxBid = viewModel.calculateMaxBid(currentBidder)

            AuctionHeader(
                auctionState = auctionState,
                currentBidder = currentBidder,
                currentPlayer = currentPlayer,
                currentBidAmount = currentBidAmount,
                initialBid = initialBid,
                isFirstBidderInRound = isFirstBidderInRound,
                maxBid = maxBid,
                countdownTime = countdownTime
            )

            BidControls(
                showBidDialog = showBidDialog,
                isPlaceBidEnabled = !hasSkipped && canAffordBid && remainingPlayers.isNotEmpty(),
                isSkipTurnEnabled = !hasPlacedBid && !hasSkipped && remainingPlayers.isNotEmpty(),
                canAffordBid = canAffordBid,
                onSkip = onSkip,
                currentPlayer = currentPlayer,
                auctionState = auctionState,
                teamDecisions = teamDecisions,
                currentBidder = currentBidder,
                onBid = onBid,
                viewModel = viewModel
            )

            PlayerAssignmentControls(
                allTeamsMadeDecision = allTeamsMadeDecision,
                showAssignCurrentDialog = showAssignCurrentDialog,
                onAssignCurrent = onAssignCurrent,
                isMoveToNextEnabled = !(!hasSkipped && canAffordBid) && !(!hasPlacedBid && !hasSkipped),
                onNext = onNext,
                auctionState = auctionState,
                showAssignRemainingDialog = showAssignRemainingDialog,
                onAssignRemaining = onAssignRemaining,
                onAssignUnsold = onAssignUnsold
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
    }
}

@Composable
private fun AuctionHeader(
    auctionState: AuctionState,
    currentBidder: String,
    currentPlayer: Player,
    currentBidAmount: Int,
    initialBid: Int,
    isFirstBidderInRound: Boolean,
    maxBid: Int,
    countdownTime: Int
) {
    SectionTitle("Auction Round ${auctionState.currentRound}")
    InfoLine("Bidding Team: $currentBidder")
    InfoLine("Bidding Player: ${getFirstName(currentPlayer.name)} (${currentPlayer.basePoint})")

    if (currentBidAmount > 0) {
        InfoLine("Last Bid Amount: $currentBidAmount")
    }

    val bidRequirementText = if (isFirstBidderInRound) {
        "Min Bid Amount Required: $initialBid"
    } else {
        val nextBid = currentBidAmount + 50
        if (nextBid <= 350) "Min Bid Amount Required: $nextBid"
        else "Bid limit reached! Cannot exceed 350."
    }
    InfoLine(bidRequirementText)
    InfoLine("Max Bid $currentBidder Can Place is $maxBid")

    CountdownTimer(countdownTime)
}

@Composable
private fun BidControls(
    showBidDialog: MutableState<Boolean>,
    isPlaceBidEnabled: Boolean,
    isSkipTurnEnabled: Boolean,
    canAffordBid: Boolean,
    onSkip: () -> Unit,
    currentPlayer: Player,
    auctionState: AuctionState,
    teamDecisions: Map<String, String>,
    currentBidder: String,
    onBid: (Int) -> Unit,
    viewModel: AuctionViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CustomButtonWithTooltip(
            text = "Place Bid",
            onClick = { showBidDialog.value = true },
            enabled = isPlaceBidEnabled,
            modifier = Modifier.weight(1f),
            backgroundColor = Color.Green,
            tooltipText = if (!canAffordBid) "Cannot place bid: Max budget reached." else null
        )

        if (showBidDialog.value) {
            BidConfirmationDialog(
                showDialog = showBidDialog,
                currentBidState = auctionState.currentBid,
                initialBidValue = currentPlayer.basePoint,
                currentBidder = currentBidder,
                teamDecisions = teamDecisions,
                onBid = onBid,
                onSkip = onSkip,
                viewModel = viewModel
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        CustomButtonWithTooltip(
            text = "Skip Turn",
            onClick = onSkip,
            enabled = isSkipTurnEnabled,
            modifier = Modifier.weight(1f),
            backgroundColor = Color.Red,
            tooltipText = if (!canAffordBid) "Cannot place bid: Max budget reached." else null
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun PlayerAssignmentControls(
    allTeamsMadeDecision: Boolean,
    showAssignCurrentDialog: MutableState<Boolean>,
    onAssignCurrent: () -> Unit,
    isMoveToNextEnabled: Boolean,
    onNext: () -> Unit,
    auctionState: AuctionState,
    showAssignRemainingDialog: MutableState<Boolean>,
    onAssignRemaining: () -> Unit,
    onAssignUnsold: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AssignmentButton(
            text = "Assign Current Player",
            enabled = allTeamsMadeDecision,
            tooltipText = if (!allTeamsMadeDecision) "Cannot assign player: No decision made" else null,
            onClick = { showAssignCurrentDialog.value = true },
            modifier = Modifier.weight(1f)
        )

        if (showAssignCurrentDialog.value) {
            ConfirmationDialog(
                showDialog = showAssignCurrentDialog,
                title = "Confirm Action",
                text = "Are you sure you want to assign the current player?",
                onConfirm = onAssignCurrent
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        CustomButtonWithTooltip(
            text = "Move to Next Team",
            onClick = onNext,
            enabled = isMoveToNextEnabled,
            modifier = Modifier.weight(1f),
            tooltipText = if (!allTeamsMadeDecision) "Current team hasn't decided" else null
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AssignmentButton(
            text = "Assign Remaining",
            enabled = auctionState.remainingPlayers.isNotEmpty(),
            tooltipText = if (auctionState.remainingPlayers.isEmpty()) "No players available" else null,
            onClick = { showAssignRemainingDialog.value = true },
            modifier = Modifier.weight(1f),
        )

        if (showAssignRemainingDialog.value) {
            ConfirmationDialog(
                showDialog = showAssignRemainingDialog,
                title = "Confirm Action",
                text = "Are you sure you want to assign remaining players?",
                onConfirm = onAssignRemaining
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        AssignmentButton(
            text = "Assign Unsold",
            enabled = auctionState.unsoldPlayers.isNotEmpty(),
            tooltipText = if (auctionState.unsoldPlayers.isEmpty()) "No unsold players" else null,
            onClick = onAssignUnsold,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BidConfirmationDialog(
    showDialog: MutableState<Boolean>,
    currentBidState: Int,
    initialBidValue: Int,
    currentBidder: String,
    teamDecisions: Map<String, String>,
    onBid: (Int) -> Unit,
    onSkip: () -> Unit,
    viewModel: AuctionViewModel
) {
    val isFirstBid = currentBidState == 0
    val newBidAmount = if (isFirstBid) {
        initialBidValue
    } else {
        (currentBidState + 50).coerceAtMost(350)
    }
    val hasPlacedBidInDialog = teamDecisions[currentBidder] == "BID"

    val message = if (isFirstBid) {
        "Congratulations!! You are placing bid on base amount $initialBidValue."
    } else {
        "You are placing a bid for $newBidAmount. Do you want to continue?"
    }

    CustomAlertDialog(
        showDialog = showDialog,
        title = "Confirm Bid",
        text = message,
        confirmText = "Yes, Place Bid",
        secondaryButtonText = if (hasPlacedBidInDialog) "Stay on Current" else "No, Skip",
        onConfirm = {
            onBid(newBidAmount)
            showDialog.value = false
        },
        onSecondary = {
            if (hasPlacedBidInDialog) viewModel.stayOnCurrentBid() else onSkip()
        },
        onDismiss = { showDialog.value = false }
    )
}

@Composable
private fun ConfirmationDialog(
    showDialog: MutableState<Boolean>,
    title: String,
    text: String,
    onConfirm: () -> Unit
) {
    CustomAlertDialog(
        showDialog = showDialog,
        title = title,
        text = text,
        confirmText = "Confirm",
        onConfirm = {
            onConfirm()
            showDialog.value = false
        },
        onDismiss = { showDialog.value = false }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        style = TextStyle(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    )
}

@Composable
private fun InfoLine(text: String, color: Color = Color.Black) {
    Text(
        text = text,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        color = color
    )
}

@Composable
private fun CountdownTimer(countdownTime: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text("Time Remaining: ", fontSize = 18.sp)
        Text("$countdownTime ", fontSize = 18.sp, color = Color.Red)
        Text("seconds", fontSize = 18.sp)
    }
}

@Composable
private fun AssignmentButton(
    text: String,
    enabled: Boolean,
    tooltipText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CustomButtonWithTooltip(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        tooltipText = tooltipText
    )
}