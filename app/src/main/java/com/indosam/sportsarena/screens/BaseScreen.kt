package com.indosam.sportsarena.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indosam.sportsarena.R

@Composable
fun BaseScreen(
    title: String,
    navController: NavController,
    showBackButton: Boolean = true,
    showHomeButton: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.app_screen_bg),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f)
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .shadow(6.dp, shape = RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.semantics { contentDescription = "Navigate back" }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icc_logo),
                        contentDescription = "Secondary Logo",
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 4.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.icc_banner),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .height(30.dp)
                    )
                }

                if (showHomeButton) {
                    IconButton(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier.semantics { contentDescription = "Navigate to home" }
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content
            content()
        }
    }
}