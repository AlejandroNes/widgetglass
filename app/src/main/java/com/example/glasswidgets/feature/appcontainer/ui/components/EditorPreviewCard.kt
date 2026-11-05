package com.example.glasswidgets.feature.appcontainer.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.domain.InstalledApp
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditorPreviewCard(
    config: WidgetVisualConfig,
    layoutType: WidgetLayoutType,
    selectedPackages: List<String>,
    installedApps: List<InstalledApp>,
    batteryLevel: Int = 100,
    batteryStatus: String = "Cargando",
    modifier: Modifier = Modifier
) {
    val (widthDp, heightDp) = when (layoutType) {
        WidgetLayoutType.HORIZONTAL_3 -> Pair(260, 85)
        WidgetLayoutType.SQUARE_4 -> Pair(160, 160)
        WidgetLayoutType.SQUARE_9 -> Pair(220, 220)
        WidgetLayoutType.CLOCK_2X1 -> Pair(150, 75)
        WidgetLayoutType.CLOCK_4X2 -> Pair(290, 140)
        WidgetLayoutType.BATTERY_2X1 -> Pair(150, 75)
        WidgetLayoutType.BATTERY_4X2 -> Pair(290, 140)
        WidgetLayoutType.CALENDAR_2X2 -> Pair(150, 150)
        WidgetLayoutType.CALENDAR_4X4 -> Pair(280, 280)
    }

    val density = LocalDensity.current.density
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bgBitmap = remember(config, layoutType, density) {
        DrawableUtils.createGlassBackgroundBitmap(
            widthPx = widthPx,
            heightPx = heightPx,
            baseColor = config.baseColor,
            opacity = config.opacity,
            cornerRadiusDp = config.cornerRadius
        ).asImageBitmap()
    }

    val wallpaperGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1B2430),
                Color(0xFF2B3A4E)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(wallpaperGradient),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bgBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            if (layoutType.isClock) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val baseTimeSize = if (layoutType == WidgetLayoutType.CLOCK_4X2) 56f else 32f
                    val baseDateSize = if (layoutType == WidgetLayoutType.CLOCK_4X2) 16f else 12f

                    val context = LocalContext.current
                    val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
                    val dateFormat = DateFormat.getDateInstance(DateFormat.FULL)
                    val now = Date()

                    Text(
                        text = timeFormat.format(now),
                        color = Color(config.textColor),
                        fontSize = TextUnit(baseTimeSize * config.clockSizeMultiplier, TextUnitType.Sp),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Light
                    )

                    if (config.showDate) {
                        Text(
                            text = dateFormat.format(now),
                            color = Color(config.textColor).copy(alpha = 0.8f),
                            fontSize = TextUnit(baseDateSize * config.clockSizeMultiplier, TextUnitType.Sp),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else if (layoutType.isBattery) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val basePercentSize = if (layoutType == WidgetLayoutType.BATTERY_4X2) 48f else 28f
                    val baseStatusSize = if (layoutType == WidgetLayoutType.BATTERY_4X2) 16f else 12f

                    if (config.showBatteryPercentage) {
                        Text(
                            text = "$batteryLevel%",
                            color = Color(config.textColor),
                            fontSize = TextUnit(basePercentSize * config.clockSizeMultiplier, TextUnitType.Sp),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = batteryStatus,
                        color = Color(config.textColor).copy(alpha = 0.8f),
                        fontSize = TextUnit(baseStatusSize * config.clockSizeMultiplier, TextUnitType.Sp),
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    val barWidthPx = (if (layoutType == WidgetLayoutType.BATTERY_4X2) 200 else 100) * density
                    val barHeightPx = 8 * density
                    val batteryBitmap = remember(config.batteryIndicatorColor, batteryLevel, density) {
                        DrawableUtils.createBatteryBarBitmap(
                            barWidthPx.toInt(), barHeightPx.toInt(), batteryLevel / 100f, config.batteryIndicatorColor, 4f
                        ).asImageBitmap()
                    }
                    Image(
                        bitmap = batteryBitmap,
                        contentDescription = null,
                        modifier = Modifier.size(
                            width = (if (layoutType == WidgetLayoutType.BATTERY_4X2) 200 else 100).dp,
                            height = 8.dp
                        )
                    )
                }
            } else if (layoutType.isCalendar) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val now = Date()
                    if (layoutType == WidgetLayoutType.CALENDAR_2X2) {
                        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                        val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                        val cal = Calendar.getInstance()
                        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

                        Text(
                            text = monthFormat.format(now).uppercase(),
                            color = Color(config.accentColor),
                            fontSize = TextUnit(14f * config.clockSizeMultiplier, TextUnitType.Sp),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentDay.toString(),
                            color = Color(config.textColor),
                            fontSize = TextUnit(48f * config.clockSizeMultiplier, TextUnitType.Sp),
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = dayNameFormat.format(now).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            color = Color(config.textColor),
                            fontSize = TextUnit(14f * config.clockSizeMultiplier, TextUnitType.Sp)
                        )
                    } else {
                        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        Text(
                            text = monthYearFormat.format(now).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            color = Color(config.textColor),
                            fontSize = TextUnit(18f * config.clockSizeMultiplier, TextUnitType.Sp),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        for (r in 0..5) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (c in 0..6) {
                                    val isToday = r == 2 && c == 3
                                    Text(
                                        text = "${r * 7 + c + 1}".takeIf { r * 7 + c < 31 } ?: "",
                                        color = if (isToday) Color(config.accentColor) else Color(config.textColor),
                                        fontSize = TextUnit(10f * config.clockSizeMultiplier, TextUnitType.Sp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                    val rows = when (layoutType) {
                        WidgetLayoutType.HORIZONTAL_3 -> 1
                        WidgetLayoutType.SQUARE_4 -> 2
                        WidgetLayoutType.SQUARE_9 -> 3
                        else -> 1
                    }
                    val cols = when (layoutType) {
                        WidgetLayoutType.SQUARE_4 -> 2
                        else -> 3
                    }

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0 until cols) {
                                val i = r * cols + c
                                val pkg = selectedPackages.getOrNull(i)
                                val app = installedApps.find { it.packageName == pkg }
                                if (app != null) {
                                    val bitmap = remember(app.icon, config.iconScale) {
                                        DrawableUtils.toBitmap(app.icon, 144, 144, config.iconScale).asImageBitmap()
                                    }
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
