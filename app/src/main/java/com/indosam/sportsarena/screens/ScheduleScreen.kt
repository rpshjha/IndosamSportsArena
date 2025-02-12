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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indosam.sportsarena.R

data class ScheduleData(
    val eventName: String,
    val date: String,
    val venue: String,
    val matches: List<String> = emptyList() // Optional match list, only for specific cards
)

@Composable
fun ScheduleScreen(navController: NavController) {
    val schedules = listOf(
        ScheduleData(
            "Indosam Box League 11",
            "03rd March, 2025",
            "PlayAll Sports Complex, Sec 73",
            listOf(
                "Match 1: Indosam Titans vs Indosam Warriors",
                "Match 2: Indosam Warriors vs Indosam Strikers",
                "Match 3: Indosam Strikers vs Indosam Titans"
            )
        ),
        ScheduleData("Indosam Box League 12", "10th March, 2025", "Indosam Sports Arena, Sec 50"),
        ScheduleData("Indosam Box League 13", "17th March, 2025", "SkyHigh Turf, Noida")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with Transparency
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f // Adjust transparency level
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Home Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Blue)
                }
            }

            // Title Below Home Button
            // Title Below Home Button
            Text(
                text = "Upcoming Schedule",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .border(width = 2.dp, color = Color.Black) // Adding border
                    .padding(12.dp), // Padding inside the border
                color = Color.Black
            )


            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(schedules) { schedule ->
                    ScheduleCard(schedule = schedule)
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(schedule: ScheduleData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = schedule.eventName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${schedule.date}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Text(
                text = "Venue: ${schedule.venue}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            // Only show matches for the first event
            if (schedule.matches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Matches:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                schedule.matches.forEach { match ->
                    Text(
                        text = match,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
