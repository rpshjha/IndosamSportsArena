package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.models.Player

@Composable
fun UnsoldPlayersList(unsoldPlayers: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        unsoldPlayers.forEach { player ->
            Text(
                text = "${getFirstName(player.name)} (${player.basePoint})",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
        }
    }
}