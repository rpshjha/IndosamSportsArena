package com.indosam.sportsarena.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.indosam.sportsarena.components.CustomButton
import com.indosam.sportsarena.models.Team
import com.indosam.sportsarena.utils.JsonUtils
import com.indosam.sportsarena.viewmodels.AuctionViewModel


@Composable
fun StartAuction(navController: NavController, viewModel: AuctionViewModel = viewModel()) {
    val context = LocalContext.current
    val teams = remember { mutableStateOf(JsonUtils.loadTeamsFromJson(context)) }
    val players by viewModel.players.collectAsState()
    val selectedPlayers by viewModel.selectedPlayers.collectAsState()
    val selectedTeamInfo by viewModel.selectedTeamInfo.collectAsState()

    // State to manage captain and vice-captain selections for each team
    val teamSelections = remember {
        mutableStateMapOf<String, Pair<String, String>>().apply {
            teams.value.forEach { team ->
                this[team] = Pair("", "")
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadPlayers() }

    BaseScreen(
        title = "Welcome to the Auction",
        navController = navController,
        showBackButton = true,
        showHomeButton = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuctionInfoButton(navController)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teams.value.size) { index ->
                    TeamCard(
                        team = Team(teams.value[index]),
                        allPlayers = players.map { it.name },
                        selectedPlayers = selectedPlayers,
                        captain = teamSelections[teams.value[index]]?.first ?: "",
                        viceCaptain = teamSelections[teams.value[index]]?.second ?: "",
                        onSelectionChanged = { captain, viceCaptain ->
                            teamSelections[teams.value[index]] = Pair(captain, viceCaptain)
                            viewModel.updateSelectedTeamInfo(
                                teams.value[index],
                                captain,
                                viceCaptain
                            )
                        },
                        onPlayerSelected = viewModel::addSelectedPlayer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = "Start Auction",
                onClick = { navigateToAuctionFlow(navController, selectedTeamInfo) },
                enabled = isAuctionReady(selectedTeamInfo, teams.value.size)
            )
        }
    }
}

@Composable
fun TeamCard(
    team: Team,
    allPlayers: List<String>,
    selectedPlayers: Set<String>,
    captain: String,
    viceCaptain: String,
    onSelectionChanged: (String, String) -> Unit,
    onPlayerSelected: (String) -> Unit
) {
    val availablePlayers by remember(selectedPlayers, allPlayers) {
        mutableStateOf(allPlayers.filterNot { it in selectedPlayers })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = team.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            DropdownMenuComponent("Select Captain", captain, availablePlayers) { newCaptain ->
                onSelectionChanged(newCaptain, viceCaptain)
                onPlayerSelected(newCaptain)
            }
            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuComponent(
                "Select Vice-Captain",
                viceCaptain,
                availablePlayers
            ) { newViceCaptain ->
                onSelectionChanged(captain, newViceCaptain)
                onPlayerSelected(newViceCaptain)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DropdownMenuComponent(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Text(
            text = selectedValue.ifEmpty { label },
            style = MaterialTheme.typography.bodyMedium,
            color = if (selectedValue.isNotEmpty()) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onSelectionChange(option)
                    expanded = false
                }
            )
        }
    }
}

private fun isAuctionReady(selectedTeamInfo: Map<String, Pair<String, String>>, totalTeams: Int): Boolean {
    return selectedTeamInfo.size == totalTeams &&
            selectedTeamInfo.all { it.value.first.isNotEmpty() && it.value.second.isNotEmpty() }
}


private fun navigateToAuctionFlow(
    navController: NavController,
    selectedTeamInfo: Map<String, Pair<String, String>>
) {
    val selectedTeamsJson = Gson().toJson(selectedTeamInfo)
    navController.navigate("auctionFlow/$selectedTeamsJson")
}