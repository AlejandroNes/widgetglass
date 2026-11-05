package com.example.glasswidgets.feature.appcontainer.domain

data class WidgetVisualConfig(
    val baseColor: Int = 0xFFFFFF,
    val opacity: Float = 0.2f,
    val cornerRadius: Float = 32f,
    val iconScale: Float = 1.0f,
    val showDate: Boolean = true,
    val clockSizeMultiplier: Float = 1.0f,
    val textColor: Int = 0xFFFFFF,
    val showBatteryPercentage: Boolean = true,
    val batteryIndicatorColor: Int = 0x4CAF50,
    val accentColor: Int = 0x2196F3
) {
    companion object {
        val PRESET_CLEAR = WidgetVisualConfig(baseColor = 0xFFFFFF, opacity = 0.15f, cornerRadius = 36f, iconScale = 1.0f)
        val PRESET_FROSTED = WidgetVisualConfig(baseColor = 0xFFFFFF, opacity = 0.35f, cornerRadius = 28f, iconScale = 0.90f)
        val PRESET_DARK = WidgetVisualConfig(baseColor = 0x1A1A1A, opacity = 0.45f, cornerRadius = 28f, iconScale = 0.90f)
        val PRESET_SOFT = WidgetVisualConfig(baseColor = 0xE1F5FE, opacity = 0.25f, cornerRadius = 36f, iconScale = 0.95f)
    }
}
