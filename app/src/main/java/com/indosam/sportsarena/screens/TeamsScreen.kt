package com.indosam.sportsarena.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var currentPlayerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(true) {
        viewModel.loadPlayers()
    }

    BaseScreen(title = "Indosam Premier League Players", navController = navController) {
        if (players.isEmpty()) {
            EmptyStateMessage()
        } else {
            PlayerDisplay(players, currentPlayerIndex, onNext = {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            }, onPrevious = {
                currentPlayerIndex =
                    (currentPlayerIndex - 1 + players.size) % players.size // Handle negative indices
            })
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
fun PlayerDisplay(
    players: List<Player>,
    currentPlayerIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val player = players[currentPlayerIndex]
    val age = remember(player.dob) { DateUtils.calculateAge(player.dob) }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp) // Adjust size as needed
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icons_back),
                contentDescription = "Previous Player",
                tint = Color.Black
            )
        }

        // Player Card (Centered Column)
        Column(
            modifier = Modifier.weight(1f), // Take remaining space
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            PlayerIcon(player, Modifier.size(200.dp))

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = player.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlayerDetailRow("Age", "$age yrs")
            PlayerDetailRow("Batting", "${player.battingStyle}")
            PlayerDetailRow("Bowling", "${player.bowlingStyle}")
        }


        IconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icons_forward),
                contentDescription = "Next Player",
                tint = Color.Black
            )
        }

    }
}

@Composable
fun PlayerDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontStyle = FontStyle.Italic)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = Color.LightGray,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Start
        )
    }
}


@Composable
fun PlayerIcon(player: Player, modifier: Modifier = Modifier) {
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
            modifier = modifier
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.boy),
            contentDescription = "Default Player Icon",
            modifier = modifier,
            tint = Color.Gray
        )
    }
}