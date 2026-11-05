package com.example.glasswidgets.feature.appcontainer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig

data class GlassPresetItem(
    val id: String,
    val name: String,
    val config: WidgetVisualConfig
)

@Composable
fun GlassPresetSelector(
    selectedConfig: WidgetVisualConfig,
    onPresetSelected: (WidgetVisualConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember {
        listOf(
            GlassPresetItem("clear", "Clear", WidgetVisualConfig.PRESET_CLEAR),
            GlassPresetItem("frosted", "Frosted", WidgetVisualConfig.PRESET_FROSTED),
            GlassPresetItem("dark", "Dark", WidgetVisualConfig.PRESET_DARK),
            GlassPresetItem("soft", "Soft Blue", WidgetVisualConfig.PRESET_SOFT)
        )
    }

    val density = LocalDensity.current.density
    val sampleWidthPx = (70 * density).toInt()
    val sampleHeightPx = (45 * density).toInt()

    val wallpaperGradient = remember {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1E2638), Color(0xFF2A3A52))
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Estilo Glass Base",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                val isSelected = (selectedConfig.baseColor == preset.config.baseColor) &&
                        (selectedConfig.opacity == preset.config.opacity)

                val cardShape = RoundedCornerShape(16.dp)
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                val borderWidth = if (isSelected) 2.dp else 1.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(borderWidth, borderColor, cardShape)
                        .clickable { onPresetSelected(preset.config) }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 75.dp, height = 50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(wallpaperGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        val sampleBitmap = remember(preset.config, density) {
                            DrawableUtils.createGlassBackgroundBitmap(
                                widthPx = sampleWidthPx,
                                heightPx = sampleHeightPx,
                                baseColor = preset.config.baseColor,
                                opacity = preset.config.opacity,
                                cornerRadiusDp = 10f
                            ).asImageBitmap()
                        }

                        Image(
                            bitmap = sampleBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(width = 60.dp, height = 38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
