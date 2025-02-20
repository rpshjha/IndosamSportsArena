package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.components.CustomButton
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.viewmodels.AuctionViewModel

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
    val auctionState = viewModel.auctionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val currentPlayer = remainingPlayers.firstOrNull()

        if (currentPlayer != null) {

            val initialBid = currentPlayer.basePoint
            val currentBidAmount = if (currentBid < initialBid) initialBid else currentBid

            // Display current player and bidding information
            Text(
                text = "Auction Round ${auctionState.value.currentRound}",
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {

                // Bid and Skip buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CustomButton(
                        text = "Place Bid",
                        onClick = { onBid(currentBidAmount + 50) },
                        enabled = remainingPlayers.isNotEmpty() && viewModel.canPlaceBid(
                            currentBidder,
                            currentBidAmount + 50
                        ),
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

                // Assign button
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