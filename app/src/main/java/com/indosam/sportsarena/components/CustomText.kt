package com.indosam.sportsarena.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier.padding(bottom = 8.dp),
    fontSize: Int = 16,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textDecoration: TextDecoration? = null,
    style: TextStyle
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color,
        style = TextStyle(textDecoration = textDecoration)
    )
}
