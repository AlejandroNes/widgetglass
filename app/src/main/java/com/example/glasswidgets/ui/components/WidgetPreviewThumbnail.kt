package com.example.glasswidgets.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.domain.InstalledApp
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig

@Composable
fun WidgetPreviewThumbnail(
    layoutType: WidgetLayoutType,
    modifier: Modifier = Modifier,
    config: WidgetVisualConfig = WidgetVisualConfig.PRESET_FROSTED,
    selectedPackages: List<String> = emptyList(),
    installedApps: List<InstalledApp> = emptyList()
) {
    val density = LocalDensity.current.density
    
    val (widthDp, heightDp) = when (layoutType) {
        WidgetLayoutType.HORIZONTAL_3 -> Pair(120, 42)
        WidgetLayoutType.SQUARE_4 -> Pair(75, 75)
        WidgetLayoutType.SQUARE_9 -> Pair(105, 105)
        WidgetLayoutType.CLOCK_2X1, WidgetLayoutType.BATTERY_2X1 -> Pair(85, 42)
        WidgetLayoutType.CLOCK_4X2, WidgetLayoutType.BATTERY_4X2 -> Pair(130, 65)
        WidgetLayoutType.CALENDAR_2X2 -> Pair(75, 75)
        WidgetLayoutType.CALENDAR_4X4 -> Pair(120, 120)
    }

    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bgBitmap = remember(config, layoutType, density) {
        DrawableUtils.createGlassBackgroundBitmap(
            widthPx = widthPx,
            heightPx = heightPx,
            baseColor = config.baseColor,
            opacity = config.opacity,
            cornerRadiusDp = 14f
        ).asImageBitmap()
    }

    // Fondo de presentación con gradiente sutil que emula un wallpaper elegante
    val wallpaperGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E2638),
                Color(0xFF2A3A52)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(wallpaperGradient),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp)
        ) {
            Image(
                bitmap = bgBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    layoutType.isClock -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "10:30",
                                color = Color(config.textColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light
                            )
                            if (config.showDate && layoutType == WidgetLayoutType.CLOCK_4X2) {
                                Text(
                                    text = "Lun, 3 Nov",
                                    color = Color(config.textColor).copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    layoutType.isBattery -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (config.showBatteryPercentage) {
                                Text(
                                    text = "85%",
                                    color = Color(config.textColor),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val barWidthPx = (widthDp * 0.6f * density).toInt()
                            val barHeightPx = (5 * density).toInt()
                            val batteryBitmap = remember(config.batteryIndicatorColor, density) {
                                DrawableUtils.createBatteryBarBitmap(
                                    barWidthPx, barHeightPx, 0.85f, config.batteryIndicatorColor, 3f
                                ).asImageBitmap()
                            }
                            Image(
                                bitmap = batteryBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(width = (widthDp * 0.6f).dp, height = 5.dp)
                            )
                        }
                    }
                    layoutType.isCalendar -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "NOV",
                                color = Color(config.accentColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "12",
                                color = Color(config.textColor),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        // Contenedor de Apps
                        val rows = if (layoutType == WidgetLayoutType.SQUARE_9) 3 else if (layoutType == WidgetLayoutType.SQUARE_4) 2 else 1
                        val cols = if (layoutType == WidgetLayoutType.SQUARE_4) 2 else 3

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (r in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (c in 0 until cols) {
                                        val idx = r * cols + c
                                        val pkg = selectedPackages.getOrNull(idx)
                                        val app = installedApps.find { it.packageName == pkg }
                                        if (app != null) {
                                            val bitmap = remember(app.icon) {
                                                DrawableUtils.toBitmap(app.icon, 48, 48).asImageBitmap()
                                            }
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
