package com.indosam.sportsarena.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.indosam.sportsarena.models.BoxLeagueMatchDetails
import com.indosam.sportsarena.models.BoxLeagueState
import com.indosam.sportsarena.utils.JsonUtils

@Composable
fun BoxCricketFixtures(navController: NavController, context: Context) {
    val upcomingSchedules = BoxLeagueMatchDetails(
        "Indosam Box League 12", "09th March, 2025", "PlayAll Sports Complex, Sec 73", listOf(
            "Match 1: Indosam Titans vs Indosam Warriors",
            "Match 2: Indosam Warriors vs Indosam Strikers",
            "Match 3: Indosam Strikers vs Indosam Titans"
        )
    )

    val pastSchedules = JsonUtils.loadMatchesFromJson(context)

    val pastSchedulesState =
        remember { mutableStateListOf(*pastSchedules.map { BoxLeagueState(it) }.toTypedArray()) }

    BaseScreen(title = "Box Cricket Fixtures", navController = navController) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            item {
                UpcomingScheduleCard(upcomingSchedules)
            }

            item {
                PreviousMatchesCard(pastSchedulesState)
            }
        }
    }
}

@Composable
fun UpcomingScheduleCard(upcomingSchedules: BoxLeagueMatchDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Upcoming Box Matches",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            BoxLeagueScheduleCard(
                schedule = upcomingSchedules, boxNo = 12
            )
        }
    }
}

@Composable
fun PreviousMatchesCard(pastSchedulesState: MutableList<BoxLeagueState>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Previous Box Matches",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            pastSchedulesState.forEachIndexed { index, state ->
                BoxLeagueScheduleCard(schedule = state.schedule,
                    boxNo = pastSchedulesState.size - index,
                    isExpanded = state.isExpanded,
                    onExpandToggle = {
                        val updatedList = pastSchedulesState.toMutableList()
                        updatedList[index] = state.copy(isExpanded = !state.isExpanded)
                        pastSchedulesState.clear()
                        pastSchedulesState.addAll(updatedList)
                    })
            }
        }
    }
}

@Composable
fun BoxLeagueScheduleCard(
    schedule: BoxLeagueMatchDetails,
    boxNo: Int,
    isExpanded: Boolean = true,
    onExpandToggle: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = onExpandToggle != null) { onExpandToggle?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                BoxLeagueScheduleText(schedule.eventName, 18.sp, FontWeight.Bold)
                if (onExpandToggle != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                BoxLeagueScheduleVisuals(boxNo = boxNo)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BoxLeagueScheduleText("Date: ${schedule.date}", 14.sp, FontWeight.Medium)
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
                    BoxLeagueScheduleText("Venue: ${schedule.venue}", 14.sp, FontWeight.Medium)
                }
                if (schedule.matches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BoxLeagueScheduleText("Matches:", 16.sp, FontWeight.SemiBold)
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
                                BoxLeagueScheduleText(match, 14.sp, FontWeight.Normal)
                            }
                        }
                    }
                }

                schedule.winnerCaptainImage?.let { WinnerCaptainImage(it) }
            }
        }
    }
}

@Composable
fun BoxLeagueScheduleText(
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
fun BoxLeagueScheduleVisuals(boxNo: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(id = R.drawable.icc_logo),
                contentDescription = "ICC Logo",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.icc_trophy),
                contentDescription = "Trophy",
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        getScheduleImageResource(boxNo)?.let { resourceId ->
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Schedule Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        getCaptainsImageResource(boxNo)?.let { resourceId ->
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Captains Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

@Composable
fun WinnerCaptainImage(imageResName: String) {
    getImageResourceByName(imageResName)?.let { resourceId ->
        val isVisible = remember { mutableStateOf(true) }

        AnimatedVisibility(
            visible = isVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.winning_captain_logo),
                            contentDescription = "Winning Captain Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Winner Captain",
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

fun getScheduleImageResource(boxNo: Int): Int? {
    return when (boxNo) {
        12 -> R.drawable.icc_box_league_schedule_12
        11 -> R.drawable.icc_box_league_schedule_11
        10 -> R.drawable.icc_box_league_schedule_10
        9 -> R.drawable.icc_box_league_schedule_9
        8 -> R.drawable.icc_box_league_schedule_8
        7 -> R.drawable.icc_box_league_schedule_7
        6 -> R.drawable.icc_box_league_schedule_6
        5 -> R.drawable.icc_box_league_schedule_5
        4 -> R.drawable.icc_box_league_schedule_4
        3 -> R.drawable.icc_box_league_schedule_3

        else -> null
    }
}

fun getCaptainsImageResource(boxNo: Int): Int? {
    return when (boxNo) {
        12 -> R.drawable.icc_box_league_captains_12
        11 -> R.drawable.icc_box_league_captains_11
        10 -> R.drawable.icc_box_league_captains_10
        9 -> R.drawable.icc_box_league_captains_9
        8 -> R.drawable.icc_box_league_captains_8
        7 -> R.drawable.icc_box_league_captains_7
        6 -> R.drawable.icc_box_league_captains_6
        5 -> R.drawable.icc_box_league_captains_5
        4 -> R.drawable.icc_box_league_captains_4
        3 -> R.drawable.icc_box_league_captains_3

        else -> null
    }
}

fun getImageResourceByName(name: String): Int? {
    return when (name) {
        "icc_box_league_winner_5" -> R.drawable.icc_box_league_winner_5
        "icc_box_league_winner_4" -> R.drawable.icc_box_league_winner_4
        "icc_box_league_winner_3" -> R.drawable.icc_box_league_winner_3
        "icc_box_league_winner_2" -> R.drawable.icc_box_league_winner_2
        "icc_box_league_winner_1" -> R.drawable.icc_box_league_winner_1

        else -> null
    }
}
