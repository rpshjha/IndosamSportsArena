package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.indosam.sportsarena.R
import com.indosam.sportsarena.models.Highlight


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
            HighlightsCarousel()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Photo Gallery",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            GalleryGrid()
        }
    }
}

@Composable
fun HighlightsCarousel() {
    val highlights = listOf(
        Highlight(
            R.drawable.match_highlight_1,
            "Indosam Box League 12: Auction Prep Hustle",
            "Pre Auction Gathering"
        ),
        Highlight(
            R.drawable.match_highlight_box_11, "Indosam Box League 11", "More exciting moments!"
        ),
    )

    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(highlights) { highlight ->
            HighlightCard(highlight)
        }
    }
}

@Composable
fun HighlightCard(highlight: Highlight) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(12.dp)
            .width(300.dp)
            .height(350.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = highlight.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .height(200.dp)
                    .weight(0.6f)
            ) {
                Image(
                    painter = painterResource(id = highlight.imageRes),
                    contentDescription = highlight.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clickable { showDialog = true },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = highlight.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.1f)
            )
        }
    }

    if (showDialog) {
        FullScreenDialog(highlight.imageRes) { showDialog = false }
    }
}

@Composable
fun GalleryGrid() {
    val galleryImages = listOf(
        R.drawable.match_highlight_1, R.drawable.match_highlight_box_11
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 8.dp)
    ) {
        items(galleryImages) { imageRes ->
            GalleryItem(imageRes)
        }
    }
}

@Composable
fun GalleryItem(imageRes: Int) {
    var showDialog by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Gallery Image",
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(150.dp)
            .clickable { showDialog = true },
        contentScale = ContentScale.Crop
    )

    if (showDialog) {
        FullScreenDialog(imageRes) { showDialog = false }
    }
}

@Composable
fun FullScreenDialog(imageRes: Int, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Close, contentDescription = "Close"
                    )
                }
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Full Screen Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

