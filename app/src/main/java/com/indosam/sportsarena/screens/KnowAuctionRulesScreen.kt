package com.indosam.sportsarena.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indosam.sportsarena.components.CustomButton
import com.indosam.sportsarena.utils.JsonUtils

@Composable
fun KnowAuctionRulesScreen(navController: NavController) {
    val context = LocalContext.current
    val teams = JsonUtils.loadTeamsFromJson(context)

    BaseScreen(title = "Auction Rules", navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuctionRuleSection("Default Teams")
            teams.forEachIndexed { index, team ->
                AuctionRuleItem("${index + 1}. $team") // Dynamically list teams
            }

            AuctionRuleItem("Each team has a Captain and Vice-Captain (pre-selected before the auction begins).")

            Spacer(modifier = Modifier.height(12.dp))

            AuctionRuleSection("1️⃣ Player Selection")
            AuctionRuleItem("A random player is chosen.")
            AuctionRuleItem("The starting team rotates in a clockwise manner (Warriors → Strikers → Titans).")

            Spacer(modifier = Modifier.height(12.dp))

            AuctionRuleSection("2️⃣ Bidding Process")
            AuctionRuleItem("The starting team must either bid (at least the base price) or pass.")
            AuctionRuleItem("If they bid, the next team (clockwise) must either increase by 50 points or pass.")
            AuctionRuleItem("The cycle continues until a team wins the bid or the maximum bid (350 points) is reached.")

            Spacer(modifier = Modifier.height(12.dp))

            AuctionRuleSection("3️⃣ Bid Winning Conditions")
            AuctionRuleItem("If a single highest bidder exists → They get the player.")
            AuctionRuleItem("If multiple teams reach the max bid (350) → A draw happens, and the player is assigned randomly.")

            Spacer(modifier = Modifier.height(12.dp))

            AuctionRuleSection("4️⃣ Team Budget Constraints")
            AuctionRuleItem("Each team has a budget of 1000 points for 6 players.")
            AuctionRuleItem("Every team must buy 6 players.")
            AuctionRuleItem("Minimum bid per player = 50 points.")

            Spacer(modifier = Modifier.height(12.dp))

            AuctionRuleSection("5️⃣ Unsold Players")
            AuctionRuleItem("Any unsold player is assigned randomly at the end.")

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = "Back to Auction",
                onClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun AuctionRuleSection(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Left,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun AuctionRuleItem(rule: String) {
    Text(
        text = "• $rule",
        fontSize = 16.sp,
        color = Color.Black,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
    )
}
