package com.indosam.sportsarena.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.indosam.sportsarena.R
import kotlinx.coroutines.delay

@Composable
fun ImageCarousel() {
    val images = listOf(
        R.drawable.warriors_bg_logo,
        R.drawable.titans_bg_logo,
        R.drawable.strikers_bg_logo
    )

    val pagerState = rememberPagerState(pageCount = { images.size })
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        currentIndex = pagerState.currentPage
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextIndex = (currentIndex + 1) % images.size
            pagerState.animateScrollToPage(nextIndex)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = images[page]),
                    contentDescription = "Carousel Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.8f)
                        .animateContentSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Page Indicator (Dots)
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                Alignment.CenterHorizontally
            )
        ) {
            images.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentIndex) 10.dp else 8.dp)
                        .background(
                            if (index == currentIndex) MaterialTheme.colorScheme.primary
                            else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}