package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.models.Player

@Composable
fun TeamTable(
    selectedTeamInfo: Map<String, Pair<String, String>>,
    teamPlayers: Map<String, List<Player>>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(1.dp))
    ) {
        Column(modifier = Modifier.padding(1.dp)) {
            val attributes =
                listOf("Team Name", "Captain", "VC", "P1", "P2", "P3", "P4", "P5", "P6")
            attributes.forEach { attribute ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = attribute,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Gray)
                            .padding(1.dp)
                    )
                    selectedTeamInfo.keys.forEach { team ->
                        val data = when (attribute) {
                            "Team Name" -> team
                            "Captain" -> getFirstName(selectedTeamInfo[team]?.first ?: "TBD")
                            "VC" -> getFirstName(selectedTeamInfo[team]?.second ?: "TBD")
                            in listOf("P1", "P2", "P3", "P4", "P5", "P6") -> {
                                val playerIndex = attribute.substring(1).toInt() - 1
                                teamPlayers[team]?.getOrNull(playerIndex)
                                    ?.let { getFirstName(it.name) } ?: "TBD"
                            }

                            else -> "N/A"
                        }
                        Text(
                            text = data,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Gray)
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}