package com.indosam.sportsarena.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indosam.sportsarena.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun TossDialog(
    showDialog: StateFlow<Boolean>,
    onConfirm: () -> Unit, // Callback to handle "OK" button click
    onDismiss: () -> Unit, // Callback to handle dialog dismissal
    tossWinner: String?
) {
    val showDialogState by showDialog.collectAsState()
    val scope = rememberCoroutineScope()
    var isTossInProgress by remember { mutableStateOf(true) }
    var resultDisplayed by remember { mutableStateOf(false) }
    var displayTossWinner by remember { mutableStateOf<String?>(null) }

    // Update displayTossWinner when tossWinner changes
    LaunchedEffect(tossWinner) {
        if (tossWinner != null) {
            displayTossWinner = tossWinner
        }
    }

    // Simulate the toss process
    LaunchedEffect(showDialogState) {
        if (showDialogState) {
            scope.launch {
                delay(3000) // Simulate toss happening for 3 seconds
                isTossInProgress = false
                resultDisplayed = true
            }
        }
    }

    if (showDialogState) {
        val scale = remember { Animatable(0.8f) }

        LaunchedEffect(true) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }

        AlertDialog(
            onDismissRequest = {
                onDismiss() // Call the onDismiss callback to close the dialog
            },
            confirmButton = {
                if (resultDisplayed) {
                    Button(
                        onClick = {
                            onConfirm() // Call the onConfirm callback when "OK" is clicked
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Text(
                    text = "Toss Result",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.toss),
                        contentDescription = "Toss Winner",
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale.value)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isTossInProgress) {
                        Text(
                            text = "All eligible teams have placed the maximum bid. Performing toss...",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                    } else if (resultDisplayed && displayTossWinner != null) {
                        Text(
                            text = "$displayTossWinner won the toss and will get the player!",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}