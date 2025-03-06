package com.indosam.sportsarena.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indosam.sportsarena.R
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    LocalContext.current

    Scaffold(bottomBar = {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color.Black
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavigationBarItem(icon = { Icon(painterResource(R.drawable.ic_teams), "Teams") },
                    selected = false,
                    onClick = { navController.navigate("teams") },
                    label = { Text("Teams") })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_schedule),
                        "Schedule"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("schedule") },
                    label = { Text("Schedule") })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_auction),
                        "Auction"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("auction") },
                    label = { Text("Auction") })
                NavigationBarItem(icon = {
                    Icon(
                        painterResource(R.drawable.ic_gallery),
                        "Gallery"
                    )
                },
                    selected = false,
                    onClick = { navController.navigate("gallery") },
                    label = { Text("Gallery") })
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
                Brush.radialGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, Color.White) as List<Color>,
                )
            )
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ImageCarousel()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, color = Color.Black
                ), textAlign = TextAlign.Center
            )

            Text(
                text = "Indosam Cricket Club", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.primary
                ), textAlign = TextAlign.Center
            )

            LogoAndTrophySection()

            Spacer(modifier = Modifier.weight(1f))

            SocialMediaLink()
        }
    }
}


@Composable
fun ImageCarousel() {
    val images = listOf(
        R.drawable.warriors_bg_logo, R.drawable.titans_bg_logo, R.drawable.strikers_bg_logo
    )

    val pagerState = rememberPagerState(pageCount = { images.size })
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        currentIndex = pagerState.currentPage
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextIndex = (currentIndex + 1) % images.size
            pagerState.animateScrollToPage(nextIndex)
        }
    }

    HorizontalPager(
        state = pagerState, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) { page ->
        Image(
            painter = painterResource(id = images[page]),
            contentDescription = "Carousel Image",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun LogoAndTrophySection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icc_logo),
                contentDescription = "ICC Logo",
                modifier = Modifier
                    .size(width = 100.dp, height = 150.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.icc_trophy),
                contentDescription = "ICC Trophy",
                modifier = Modifier
                    .size(width = 100.dp, height = 150.dp)
            )
        }
    }
}

@Composable
fun SocialMediaLink() {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/indosamcricket/")
                )
                context.startActivity(intent)
            }
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Follow us on",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(id = R.drawable.icons_instagram),
            contentDescription = "Instagram Icon",
            modifier = Modifier.size(24.dp)
        )
    }
}