package com.indosam.sportsarena.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomButtonWithTooltip(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(64.dp),
    shape: Shape = CircleShape,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    fontSize: Int = 20,
    tooltipText: String?,
) {
    val tooltipState = rememberTooltipState()

    Box(modifier = modifier) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                if (!tooltipText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tooltipText,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            state = tooltipState
        ) {
            CustomButton(
                text = text,
                onClick = { if (enabled) onClick() },
                enabled = enabled,
                modifier = Modifier,
                backgroundColor = backgroundColor,
                shape = shape,
                textColor = textColor,
                fontSize = fontSize
            )
        }
    }
}
