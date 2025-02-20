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

@Composable
fun PointsTable(teamBudgets: Map<String, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(1.dp))
    ) {
        Column(modifier = Modifier.padding(1.dp)) {
            val attributes = listOf("Points Utilised", "Points Left", "Total Budget")
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
                    teamBudgets.keys.forEach { team ->
                        val data = when (attribute) {
                            "Points Utilised" -> (1000 - (teamBudgets[team] ?: 1000)).toString()
                            "Points Left" -> (teamBudgets[team] ?: 1000).toString()
                            "Total Budget" -> "1000"
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