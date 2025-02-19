package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.indosam.sportsarena.R
import com.indosam.sportsarena.components.CustomButton

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.home_bg),
            contentDescription = "Background Image",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .graphicsLayer(alpha = 0.5f)
        )

        // Semi-transparent overlay for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(64.dp))

            // Welcome Text (More prominent)
            Text(
                text = "Welcome to Indosam Sports Arena",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CustomButton(
                            text = "Auction",
                            onClick = { navController.navigate("auction") })
                        Spacer(modifier = Modifier.height(18.dp))
                        CustomButton(text = "Know Teams", { navController.navigate("teams") })
                        Spacer(modifier = Modifier.height(18.dp))
                        CustomButton(
                            text = "Upcoming Box Schedule",
                            onClick = { navController.navigate("schedule") })
                    }
                }
            }
        }
    }
}