package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indosam.sportsarena.R

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
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ScheduleFooter()
            }

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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                ScheduleText(schedule.eventName, 18.sp, FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                ScheduleText("Date: ${schedule.date}", 14.sp, FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Venue",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                ScheduleText("Venue: ${schedule.venue}", 14.sp, FontWeight.Medium)
            }

            if (schedule.matches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ScheduleText("Matches:", 16.sp, FontWeight.SemiBold)
                Column {
                    schedule.matches.forEach { match ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sports,
                                contentDescription = "Match",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ScheduleText(match, 14.sp, FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
fun ScheduleFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
            Image(
                painter = painterResource(id = R.drawable.icc_logo),
                contentDescription = "ICC Logo",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.trophy),
                contentDescription = "Trophy",
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.schedule_banner),
            contentDescription = "Schedule Banner",
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.captains),
            contentDescription = "Captains Image",
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

    }
}
