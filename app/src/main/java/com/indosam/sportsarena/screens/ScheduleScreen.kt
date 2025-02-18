package com.indosam.sportsarena.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class ScheduleData(
    val eventName: String,
    val date: String,
    val venue: String,
    val matches: List<String> = emptyList()
)

@Composable
fun ScheduleScreen(navController: NavController) {
    val schedules = listOf(
        ScheduleData(
            "Indosam Box League 12", "09th March, 2025", "PlayAll Sports Complex, Sec 73",
            listOf(
                "Match 1: Indosam Titans vs Indosam Warriors",
                "Match 2: Indosam Warriors vs Indosam Strikers",
                "Match 3: Indosam Strikers vs Indosam Titans"
            )
        )
    )

    BaseScreen(title = "Upcoming Schedule", navController = navController) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            items(schedules, key = { it.eventName }) { schedule ->
                ScheduleCard(schedule)
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
        Column(modifier = Modifier.padding(16.dp)) {
            ScheduleText(schedule.eventName, 18.sp, FontWeight.Bold)
            ScheduleText("Date: ${schedule.date}", 14.sp, FontWeight.Medium, Color.DarkGray)
            ScheduleText("Venue: ${schedule.venue}", 14.sp, FontWeight.Medium, Color.DarkGray)

            if (schedule.matches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ScheduleText("Matches:", 16.sp, FontWeight.SemiBold)
                Column {
                    schedule.matches.forEach { match ->
                        ScheduleText(match, 14.sp, FontWeight.Normal, Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleText(text: String, fontSize: TextUnit, fontWeight: FontWeight, color: Color = Color.Black) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}
