package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.R
import com.indosam.sportsarena.models.Team
import com.indosam.sportsarena.viewmodels.AuctionViewModel

@Composable
fun AuctionScreen(navController: NavController, viewModel: AuctionViewModel = viewModel()) {
    val players by viewModel.players.collectAsState()
    val teams = listOf(Team("Indosam Warriors"), Team("Indosam Strikers"), Team("Indosam Titans"))

    val selectedTeamInfo = remember { mutableStateOf(mapOf<String, Pair<String, String>>()) }

    LaunchedEffect(Unit) { viewModel.loadPlayers() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Home Button, Title, and Team List code...
            // Home Button

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Blue)
                }
            }

            // Title

            Text(
                text = "Welcome to the Auction",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.border(2.dp, Color.Black).padding(12.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // List of Teams

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teams.size) { index ->
                    TeamCard(teams[index].name, players.map { it.name }) { captain, viceCaptain ->
                        selectedTeamInfo.value =
                            selectedTeamInfo.value +
                                    (teams[index].name to Pair(captain, viceCaptain))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Auction Button
            Button(
                onClick = {
                    // Serialize the selectedTeamInfo map to JSON
                    val gson = Gson()
                    val selectedTeamsJson = gson.toJson(selectedTeamInfo.value)

                    println(
                        "Debug: Serialized JSON being sent to the next screen: $selectedTeamsJson"
                    )

                    val deserializedMap =
                        gson.fromJson<Map<String, Pair<String, String>>>(
                            selectedTeamsJson,
                            object : TypeToken<Map<String, Pair<String, String>>>() {}.type
                        )
                    println("Debug: Deserialized map for verification: $deserializedMap")

                    // Navigate to the next screen with all teams' information
                    navController.navigate("auctionFlow/$selectedTeamsJson")
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = selectedTeamInfo.value.isNotEmpty()
            ) {
                Text(text = "Start Auction", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TeamCard(
    teamName: String,
    allPlayers: List<String>,
    onSelectionChanged: (String, String) -> Unit
) {
    var captain by remember { mutableStateOf("") }
    var viceCaptain by remember { mutableStateOf("") }
    var selectedPlayers = remember { mutableStateOf(mutableSetOf<String>()) }

    // Filter available players (remove selected captain & vice-captain)
    val availablePlayers = allPlayers.filter { it !in selectedPlayers.value }

    LaunchedEffect(captain, viceCaptain) {
        if (captain.isNotEmpty() && viceCaptain.isNotEmpty() && captain == viceCaptain) {
            // Reset vice-captain if it matches the captain
            viceCaptain = ""
        }
        onSelectionChanged(captain, viceCaptain)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = teamName, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            // Select Captain Dropdown
            DropdownMenuComponent(
                label = "Select Captain",
                selectedValue = captain,
                options = availablePlayers,
                onSelectionChange = {
                    captain = it
                    selectedPlayers.value.add(
                        it
                    ) // Mark this player as selected // Ensure vice-captain can be selected again
                    // if needed
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Select Vice-Captain Dropdown
            DropdownMenuComponent(
                label = "Select Vice-Captain",
                selectedValue = viceCaptain,
                options = availablePlayers,
                onSelectionChange = {
                    viceCaptain = it
                    selectedPlayers.value.add(it) // Mark this player as selected
                }
            )

            Spacer(modifier = Modifier.height(8.dp)) // Prevents overlap
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
    var selectedText by remember { mutableStateOf(selectedValue) }

    // Button or Text that triggers the dropdown
    Box(
        modifier =
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded } // Toggle dropdown on click
            .padding(16.dp)
    ) {
        Text(
            text = selectedText.ifEmpty { label },
            style = MaterialTheme.typography.bodyMedium,
            color =
            if (selectedText.isNotEmpty()) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }

    // The actual dropdown menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false } // Dismiss the menu when clicked outside
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    selectedText = option
                    onSelectionChange(option)
                    expanded = false // Close the menu after selection
                }
            )
        }
    }
}
