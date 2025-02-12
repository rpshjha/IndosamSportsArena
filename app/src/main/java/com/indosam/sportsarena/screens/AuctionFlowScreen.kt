package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.R
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.viewmodels.AuctionViewModel

@Composable
fun AuctionFlowScreen(
    navController: NavController,
    selectedTeamsJson: String,
    viewModel: AuctionViewModel = viewModel()
) {
    // Deserialize the JSON string back into a map
    val gson = Gson()
    val type = object : TypeToken<Map<String, Pair<String, String>>>() {}.type
    val selectedTeamInfo = gson.fromJson<Map<String, Pair<String, String>>>(selectedTeamsJson, type)

    // Extract captain and vice-captain names to exclude them from remaining players
    val excludeNames = selectedTeamInfo.values.flatMap { listOf(it.first, it.second) }

    // State for auction flow
    val teams = selectedTeamInfo.keys.toList()
    var currentBid by remember { mutableIntStateOf(0) } // Current bid for the player
    var currentBidder by remember { mutableStateOf(teams.random()) } // Random starting team
    var auctionStatus by remember { mutableStateOf("Auction in progress...") }
    var remainingPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    val teamBudgets by remember { mutableStateOf(teams.associateWith { 1000 }.toMutableMap()) }
    val teamPlayers by remember {
        mutableStateOf(teams.associateWith { mutableListOf<Player>() }.toMutableMap())
    }

    // Observe players from ViewModel
    val playersState = viewModel.players.collectAsState()

    // Load players when the screen is launched
    LaunchedEffect(Unit) {
        remainingPlayers = playersState.value.filter { it.name !in excludeNames }
    }

    // Update remainingPlayers when playersState changes
    LaunchedEffect(playersState.value) { remainingPlayers = playersState.value }

    // Function to handle bidding
    fun handleBid(team: String, bid: Int) {
        val player = remainingPlayers.firstOrNull()
        if (player == null) {
            auctionStatus = "No players remaining!"
            return
        }
        if (bid < player.basePoint) {
            auctionStatus = "Bid must be at least ${player.basePoint} points!"
            return
        }
        if (bid > teamBudgets[team]!!) {
            auctionStatus = "$team cannot afford this bid!"
            return
        }
        if (bid < currentBid + 50) {
            auctionStatus = "Bid must be increased by at least 50 points!"
            return
        }
        currentBid = bid
        currentBidder = team
        auctionStatus = "$team bids $bid points for ${player.name}!"
    }

    // Function to assign a player to the winning team
    fun assignPlayer() {
        val player = remainingPlayers.firstOrNull()
        if (player != null) {
            teamPlayers[currentBidder]?.add(player)
            teamBudgets[currentBidder] = teamBudgets[currentBidder]!! - currentBid
            remainingPlayers = remainingPlayers.drop(1)
            auctionStatus = "${player.name} assigned to $currentBidder for $currentBid points!"
            currentBid = 0 // Reset bid for the next player
            currentBidder =
                teams[(teams.indexOf(currentBidder) + 1) % teams.size] // Move to the next team
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Home and Back Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Blue)
                }
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Blue)
                }
            }

            // Page Title
            Text(
                text = "Auction Flow",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.border(2.dp, Color.Black).padding(12.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Teams Information in Tabular Format
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                selectedTeamInfo.entries.forEach { (teamName, captainViceCaptain) ->
                    val (captain, viceCaptain) = captainViceCaptain
                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Team Name
                        Text(
                            text = teamName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Captain and Vice Captain
                        Text(
                            text = captain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = viceCaptain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Players
                        teamPlayers[teamName]?.forEach { player ->
                            Text(
                                text = "${player.name} (${player.basePoint})",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        repeat(6 - (teamPlayers[teamName]?.size ?: 0)) {
                            Text(
                                text = "TBD",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Budget
                        Text(
                            text = "Budget: ${teamBudgets[teamName]} points",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auction Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = auctionStatus,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Bid Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    teams.forEach { team ->
                        Button(
                            onClick = { handleBid(team, currentBid + 50) },
                            enabled = currentBidder == team && remainingPlayers.isNotEmpty()
                        ) {
                            Text(text = "$team Bid ${currentBid + 50}")
                        }
                    }
                }

                // Assign Player Button
                Button(
                    onClick = { assignPlayer() },
                    enabled = remainingPlayers.isNotEmpty(),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Assign Player")
                }
            }
        }
    }
}
