package com.indosam.sportsarena.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.indosam.sportsarena.R
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.utils.DateUtils
import com.indosam.sportsarena.viewmodels.AuctionViewModel

@Composable
fun TeamsScreen(navController: NavController, viewModel: AuctionViewModel = viewModel()) {
    val players by viewModel.players.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadPlayers()
    }

    BaseScreen(title = "Indosam Premier League Players", navController = navController) {
        if (players.isEmpty()) {
            EmptyStateMessage()
        } else {
            PlayerList(players)
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏏 No players available!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = "Check back later for updates.",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}

@Composable
fun PlayerList(players: List<Player>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(players, key = { it.id }) { player ->
            PlayerCard(player)
        }
    }
}

@Composable
fun PlayerCard(player: Player) {
    val age = remember(player.dob) { DateUtils.calculateAge(player.dob) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Player Icon
            PlayerIcon(player)

            // Player details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = player.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Age: $age",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Batting Style: ${player.battingStyle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Bowling Style: ${player.bowlingStyle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


@Composable
fun PlayerIcon(player: Player) {
    val context = LocalContext.current

    val iconResId = remember(player.icon) {
        if (player.icon != null) {
            context.resources.getIdentifier(player.icon, "drawable", context.packageName)
        } else {
            0
        }
    }

    if (iconResId != 0) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Player Icon",
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default Player Icon",
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp),
            tint = Color.Gray
        )
    }
}

