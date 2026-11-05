package com.example.glasswidgets.feature.appcontainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ColorPresetSelector(
    title: String,
    selectedColorInt: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorOptions = remember {
        listOf(
            0xFFFFFFFF.toInt(), // Blanco
            0xFF2196F3.toInt(), // Azul
            0xFF4CAF50.toInt(), // Verde
            0xFFFF9800.toInt(), // Naranja
            0xFFE91E63.toInt(), // Rosa
            0xFF9C27B0.toInt(), // Púrpura
            0xFF121212.toInt()  // Negro Oscuro
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(colorOptions) { colorInt ->
                val isSelected = selectedColorInt == colorInt
                val color = Color(colorInt)
                val checkColor = if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorInt) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = "✓",
                            color = checkColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
