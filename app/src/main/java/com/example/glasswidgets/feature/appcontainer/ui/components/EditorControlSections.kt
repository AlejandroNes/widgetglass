package com.example.glasswidgets.feature.appcontainer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.domain.InstalledApp
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig

@Composable
fun BaseVisualControls(
    config: WidgetVisualConfig,
    onOpacityChanged: (Float) -> Unit,
    onRadiusChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Transparencia: ${(config.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Slider(value = config.opacity, onValueChange = onOpacityChanged, valueRange = 0f..1f)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text("Esquinas: ${config.cornerRadius.toInt()}dp", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Slider(value = config.cornerRadius, onValueChange = onRadiusChanged, valueRange = 0f..64f)
    }
}

@Composable
fun ClockEditorSection(
    config: WidgetVisualConfig,
    onClockSizeChanged: (Float) -> Unit,
    onShowDateChanged: (Boolean) -> Unit,
    onTextColorSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Ajustes de Reloj", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Tamaño del Texto: ${(config.clockSizeMultiplier * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(value = config.clockSizeMultiplier, onValueChange = onClockSizeChanged, valueRange = 0.5f..2.0f)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Mostrar Fecha", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = config.showDate, onCheckedChange = onShowDateChanged)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        ColorPresetSelector(
            title = "Color de Texto",
            selectedColorInt = config.textColor,
            onColorSelected = onTextColorSelected
        )
    }
}

@Composable
fun BatteryEditorSection(
    config: WidgetVisualConfig,
    onSizeChanged: (Float) -> Unit,
    onShowPercentageChanged: (Boolean) -> Unit,
    onIndicatorColorSelected: (Int) -> Unit,
    onTextColorSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Ajustes de Batería", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Escala del Indicador: ${(config.clockSizeMultiplier * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(value = config.clockSizeMultiplier, onValueChange = onSizeChanged, valueRange = 0.5f..2.0f)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Mostrar Porcentaje Numérico", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = config.showBatteryPercentage, onCheckedChange = onShowPercentageChanged)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ColorPresetSelector(
            title = "Color del Indicador de Carga",
            selectedColorInt = config.batteryIndicatorColor,
            onColorSelected = onIndicatorColorSelected
        )

        Spacer(modifier = Modifier.height(8.dp))
        ColorPresetSelector(
            title = "Color de Texto",
            selectedColorInt = config.textColor,
            onColorSelected = onTextColorSelected
        )
    }
}

@Composable
fun CalendarEditorSection(
    config: WidgetVisualConfig,
    onSizeChanged: (Float) -> Unit,
    onAccentColorSelected: (Int) -> Unit,
    onTextColorSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Ajustes de Calendario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Escala del Texto: ${(config.clockSizeMultiplier * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(value = config.clockSizeMultiplier, onValueChange = onSizeChanged, valueRange = 0.5f..2.0f)
        }

        Spacer(modifier = Modifier.height(8.dp))
        ColorPresetSelector(
            title = "Color de Acento (Día Actual)",
            selectedColorInt = config.accentColor,
            onColorSelected = onAccentColorSelected
        )

        Spacer(modifier = Modifier.height(8.dp))
        ColorPresetSelector(
            title = "Color de Texto",
            selectedColorInt = config.textColor,
            onColorSelected = onTextColorSelected
        )
    }
}

@Composable
fun AppContainerEditorSection(
    config: WidgetVisualConfig,
    layoutType: WidgetLayoutType,
    selectedPackages: List<String>,
    installedApps: List<InstalledApp>,
    onScaleChanged: (Float) -> Unit,
    onMoveApp: (Int, Int) -> Unit,
    onToggleAppSelection: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Ajustes de Contenedor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Tamaño de Iconos: ${(config.iconScale * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(value = config.iconScale, onValueChange = onScaleChanged, valueRange = 0.5f..1f)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Apps Seleccionadas (${selectedPackages.size}/${layoutType.maxApps}) - Mantén presionado para reordenar",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        ReorderableAppList(
            selectedPackages = selectedPackages,
            installedApps = installedApps,
            onMove = onMoveApp,
            onRemove = onToggleAppSelection
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aplicaciones Instaladas",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(installedApps) { app ->
                val isSelected = selectedPackages.contains(app.packageName)
                AppListItem(app = app, isSelected = isSelected) {
                    onToggleAppSelection(app.packageName)
                }
            }
        }
    }
}

@Composable
fun ReorderableAppList(
    selectedPackages: List<String>,
    installedApps: List<InstalledApp>,
    onMove: (Int, Int) -> Unit,
    onRemove: (String) -> Unit
) {
    val itemHeight = 52.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        selectedPackages.forEachIndexed { index, pkg ->
            val app = installedApps.find { it.packageName == pkg }
            if (app != null) {
                val isDragged = index == draggedIndex
                val zIndex = if (isDragged) 1f else 0f
                val offset = if (isDragged) dragOffset else 0f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .zIndex(zIndex)
                        .graphicsLayer { translationY = offset }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggedIndex = index },
                                onDragEnd = { 
                                    draggedIndex = null
                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    draggedIndex = null
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y
                                    if (dragOffset > itemHeightPx * 0.75f && index < selectedPackages.size - 1) {
                                        onMove(index, index + 1)
                                        draggedIndex = index + 1
                                        dragOffset -= itemHeightPx
                                    } else if (dragOffset < -itemHeightPx * 0.75f && index > 0) {
                                        onMove(index, index - 1)
                                        draggedIndex = index - 1
                                        dragOffset += itemHeightPx
                                    }
                                }
                            )
                        }
                        .padding(vertical = 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .border(1.dp, if(isDragged) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "::", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    
                    val bitmap = remember(app.icon) { DrawableUtils.toBitmap(app.icon, 64, 64).asImageBitmap() }
                    Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(28.dp))
                    
                    Spacer(Modifier.width(10.dp))
                    Text(text = app.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = { onRemove(app.packageName) }) { 
                        Text("✕", color = MaterialTheme.colorScheme.error) 
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: InstalledApp, isSelected: Boolean, onClick: () -> Unit) {
    val bitmap = remember(app.icon) {
        DrawableUtils.toBitmap(app.icon, 80, 80).asImageBitmap()
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() }
        )
    }
}
