package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            PlayerDisplay(
                player = players[currentPlayerIndex],
                onNext = { currentPlayerIndex = (currentPlayerIndex + 1) % players.size },
                onPrevious = {
                    currentPlayerIndex = (currentPlayerIndex - 1 + players.size) % players.size
                }
            )
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
fun NavigationButton(onClick: () -> Unit, iconResId: Int, contentDescription: String) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Composable
fun PlayerDisplay(player: Player, onNext: () -> Unit, onPrevious: () -> Unit) {
    val age = remember(player.dob) { DateUtils.calculateAge(player.dob) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Player Icon
                PlayerIcon(player, Modifier.size(150.dp))
                Spacer(modifier = Modifier.height(12.dp))

                // Player Name and Age
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = player.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$age yrs",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = player.address,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = player.aboutMe,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Player Details Table
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerDetail("Batting Style", player.battingStyle)
                        HorizontalDivider()
                        if (player.bowlingStyle.isNotEmpty()) {
                            PlayerDetail("Bowling Style", player.bowlingStyle)
                            HorizontalDivider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationButton(
                onClick = onPrevious,
                iconResId = R.drawable.icons_back,
                contentDescription = "Previous Player"
            )
            NavigationButton(
                onClick = onNext,
                iconResId = R.drawable.icons_forward,
                contentDescription = "Next Player"
            )
        }
    }
}

@Composable
fun PlayerDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun PlayerIcon(player: Player, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val iconResId = remember(player.icon) {
        if (!player.icon.isNullOrEmpty() && player.icon != "null") {
            val resId =
                context.resources.getIdentifier(player.icon, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.boy
        } else {
            R.drawable.boy
        }
    }

    Image(
        painter = painterResource(id = iconResId),
        contentDescription = "Player Icon",
        modifier = modifier
    )
}