package com.indosam.sportsarena.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.time.delay
import java.time.Duration
import kotlin.math.roundToInt

@Composable
fun NewsFlashSection() {
    val newsArticles = listOf(
        "\uD83D\uDD25 Something Big is Coming!"
    )

    var currentArticleIndex by remember { mutableIntStateOf(0) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var textWidth by remember { mutableFloatStateOf(0f) } // Actual text width
    val density = LocalDensity.current
    TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)


    val screenWidth = with(density) {
        1000.dp.toPx()
    }

    LaunchedEffect(newsArticles[currentArticleIndex], textWidth) {
        while (true) {

            offsetX = screenWidth
            animate(
                initialValue = screenWidth,
                targetValue = -textWidth,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 10000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            ) { value, _ -> offsetX = value }

            delay(Duration.ofSeconds(10))
            currentArticleIndex = (currentArticleIndex + 1) % newsArticles.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = newsArticles[currentArticleIndex],
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .onGloballyPositioned { coordinates ->

                    textWidth = coordinates.size.width.toFloat()
                },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            maxLines = 1,
            softWrap = false
        )
    }
}