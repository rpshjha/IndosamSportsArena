package com.indosam.sportsarena.screens.auction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.models.AuctionLog

@Composable
fun AuctionLogsSection(logs: List<AuctionLog>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Auction Logs",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            style = TextStyle(
                textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "No logs available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                logs.reversed().forEach { log ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Round ${log.round}: ${log.message}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                }
            }
        }
    }
}