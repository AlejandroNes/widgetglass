package com.example.glasswidgets.feature.appcontainer.data

import android.content.Context
import android.content.SharedPreferences
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig

class WidgetConfigRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("WidgetConfigPrefs", Context.MODE_PRIVATE)

    fun saveSelectedApps(widgetId: Int, packageNames: List<String>) {
        val appsString = packageNames.joinToString(",")
        prefs.edit().putString(PREF_KEY_APPS + widgetId, appsString).apply()
    }

    fun getSelectedApps(widgetId: Int): List<String> {
        val appsString = prefs.getString(PREF_KEY_APPS + widgetId, null) ?: return emptyList()
        return appsString.split(",").filter { it.isNotBlank() }
    }

    fun saveVisualConfig(widgetId: Int, config: WidgetVisualConfig) {
        val configString = "${config.baseColor},${config.opacity},${config.cornerRadius},${config.iconScale},${config.showDate},${config.clockSizeMultiplier},${config.textColor},${config.showBatteryPercentage},${config.batteryIndicatorColor},${config.accentColor}"
        prefs.edit().putString(PREF_KEY_VISUAL + widgetId, configString).apply()
    }

    fun getVisualConfig(widgetId: Int): WidgetVisualConfig {
        val str = prefs.getString(PREF_KEY_VISUAL + widgetId, null) ?: return WidgetVisualConfig.PRESET_CLEAR
        val parts = str.split(",")
        try {
            if (parts.size >= 4) {
                val showDate = if (parts.size >= 5) parts[4].toBooleanStrictOrNull() ?: true else true
                val clockSize = if (parts.size >= 6) parts[5].toFloatOrNull() ?: 1.0f else 1.0f
                val textColor = if (parts.size >= 7) parts[6].toIntOrNull() ?: 0xFFFFFF else 0xFFFFFF
                val showBat = if (parts.size >= 8) parts[7].toBooleanStrictOrNull() ?: true else true
                val batColor = if (parts.size >= 9) parts[8].toIntOrNull() ?: 0x4CAF50 else 0x4CAF50
                val accentColor = if (parts.size >= 10) parts[9].toIntOrNull() ?: 0x2196F3 else 0x2196F3
                
                return WidgetVisualConfig(
                    baseColor = parts[0].toInt(),
                    opacity = parts[1].toFloat(),
                    cornerRadius = parts[2].toFloat(),
                    iconScale = parts[3].toFloat(),
                    showDate = showDate,
                    clockSizeMultiplier = clockSize,
                    textColor = textColor,
                    showBatteryPercentage = showBat,
                    batteryIndicatorColor = batColor,
                    accentColor = accentColor
                )
            }
        } catch (e: Exception) {
            // Failsafe
        }
        return WidgetVisualConfig.PRESET_CLEAR
    }

    fun saveLayoutType(widgetId: Int, layoutType: WidgetLayoutType) {
        prefs.edit().putString(PREF_KEY_LAYOUT + widgetId, layoutType.name).apply()
    }

    fun getLayoutType(widgetId: Int): WidgetLayoutType {
        val typeStr = prefs.getString(PREF_KEY_LAYOUT + widgetId, null)
        return WidgetLayoutType.fromString(typeStr)
    }

    fun hasConfig(widgetId: Int): Boolean {
        return prefs.contains(PREF_KEY_APPS + widgetId)
    }

    fun savePendingConfig(pendingId: String, packageNames: List<String>, visualConfig: WidgetVisualConfig, layoutType: WidgetLayoutType) {
        val appsString = packageNames.joinToString(",")
        val visualString = "${visualConfig.baseColor},${visualConfig.opacity},${visualConfig.cornerRadius},${visualConfig.iconScale},${visualConfig.showDate},${visualConfig.clockSizeMultiplier},${visualConfig.textColor},${visualConfig.showBatteryPercentage},${visualConfig.batteryIndicatorColor},${visualConfig.accentColor}"
        prefs.edit()
            .putString("pending_apps_$pendingId", appsString)
            .putString("pending_visual_$pendingId", visualString)
            .putString("pending_layout_$pendingId", layoutType.name)
            .putLong("pending_time_$pendingId", System.currentTimeMillis())
            .apply()
    }

    fun saveMonthOffset(widgetId: Int, offset: Int) {
        prefs.edit().putInt("calendar_offset_$widgetId", offset).apply()
    }

    fun getMonthOffset(widgetId: Int): Int {
        return prefs.getInt("calendar_offset_$widgetId", 0)
    }

    fun consumePendingConfig(pendingId: String, targetWidgetId: Int): Boolean {
        val appsString = prefs.getString("pending_apps_$pendingId", null) ?: return false
        val visualString = prefs.getString("pending_visual_$pendingId", null)
        val layoutString = prefs.getString("pending_layout_$pendingId", null)

        prefs.edit().putString(PREF_KEY_APPS + targetWidgetId, appsString).apply()
        if (visualString != null) {
            prefs.edit().putString(PREF_KEY_VISUAL + targetWidgetId, visualString).apply()
        }
        if (layoutString != null) {
            prefs.edit().putString(PREF_KEY_LAYOUT + targetWidgetId, layoutString).apply()
        }

        prefs.edit()
            .remove("pending_apps_$pendingId")
            .remove("pending_visual_$pendingId")
            .remove("pending_layout_$pendingId")
            .remove("pending_time_$pendingId")
            .apply()
        
        return true
    }
    
    fun syncActiveWidgets(systemActiveIds: List<Int>): List<Int> {
        val storedIds = prefs.all.keys
            .filter { it.startsWith(PREF_KEY_APPS) }
            .mapNotNull { it.removePrefix(PREF_KEY_APPS).toIntOrNull() }
        
        val editor = prefs.edit()
        
        storedIds.filter { !systemActiveIds.contains(it) }.forEach { ghostId ->
            editor.remove(PREF_KEY_APPS + ghostId)
            editor.remove(PREF_KEY_VISUAL + ghostId)
            editor.remove(PREF_KEY_LAYOUT + ghostId)
        }
        
        val now = System.currentTimeMillis()
        prefs.all.keys.filter { it.startsWith("pending_time_") }.forEach { key ->
            val timestamp = prefs.getLong(key, 0L)
            if (now - timestamp > 30 * 60 * 1000) {
                val pId = key.removePrefix("pending_time_")
                editor.remove("pending_apps_$pId")
                editor.remove("pending_visual_$pId")
                editor.remove("pending_layout_$pId")
                editor.remove(key)
            }
        }
        
        editor.apply()
        return systemActiveIds.filter { storedIds.contains(it) }
    }

    fun deleteConfig(widgetId: Int) {
        prefs.edit()
            .remove(PREF_KEY_APPS + widgetId)
            .remove(PREF_KEY_VISUAL + widgetId)
            .remove(PREF_KEY_LAYOUT + widgetId)
            .remove("calendar_offset_$widgetId")
            .apply()
    }

    companion object {
        private const val PREF_KEY_APPS = "widget_apps_"
        private const val PREF_KEY_VISUAL = "widget_visual_"
        private const val PREF_KEY_LAYOUT = "widget_layout_"
    }
}
