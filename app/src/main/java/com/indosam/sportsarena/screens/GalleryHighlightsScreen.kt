package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun GalleryHighlightsScreen(navController: NavController) {
    BaseScreen(
        title = "Gallery & Highlights",
        navController = navController,
        showBackButton = true,
        showHomeButton = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HighlightCard()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Photo Gallery",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            GalleryGrid()
        }
    }
}

@Composable
fun HighlightCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Latest Highlights",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.match_highlight_1),
                contentDescription = "Highlight 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { /* Navigate to full-screen highlight */ },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🏏 Thrilling match between Indosam Warriors & Titans!",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun GalleryGrid() {
    val galleryImages = listOf(
        R.drawable.match_highlight_1,
        R.drawable.match_highlight_1,  // Replace with actual images
        R.drawable.match_highlight_1,
        R.drawable.match_highlight_1,
        R.drawable.match_highlight_1,
        R.drawable.match_highlight_1
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 8.dp)
    ) {
        items(galleryImages) { imageRes ->
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Gallery Image",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { /* Open full-screen viewer */ },
                contentScale = ContentScale.Crop
            )
        }
    }
}
