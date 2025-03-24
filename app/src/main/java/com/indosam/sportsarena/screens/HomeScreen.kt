package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indosam.sportsarena.R


@Composable
fun HomeScreen(navController: NavController) {
    LocalContext.current

    Scaffold(bottomBar = {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationBarItem(icon = { Icon(painterResource(R.drawable.ic_teams), "Teams") },
                    selected = false,
                    onClick = { navController.navigate("teams") },
                    label = { Text("Teams", maxLines = 1, overflow = TextOverflow.Ellipsis) })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_schedule), "Schedule"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("schedule") },
                    label = { Text("Schedule", maxLines = 1, overflow = TextOverflow.Ellipsis) })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_auction), "Auction"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("auction") },
                    label = { Text("Auction", maxLines = 1, overflow = TextOverflow.Ellipsis) })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_gallery), "Gallery"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("gallery") },
                    label = { Text("Gallery", maxLines = 1, overflow = TextOverflow.Ellipsis) })
            }
        }
    }) { innerPadding ->
        HomeScreenContent(innerPadding)
    }
}

@Composable
fun HomeScreenContent(innerPadding: PaddingValues) {
    LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        Color.White
                    )
                )
            )
            .padding(innerPadding)
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_screen_background),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ImageCarousel()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Color.Black
                ), textAlign = TextAlign.Center
            )

            Text(
                text = "Indosam Cricket Club", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    color = MaterialTheme.colorScheme.primary
                ), textAlign = TextAlign.Center
            )

            LogoAndTrophySection()

            Spacer(modifier = Modifier.weight(1f))

            NewsFlashSection()

            SocialMediaLink()
        }
    }
}


