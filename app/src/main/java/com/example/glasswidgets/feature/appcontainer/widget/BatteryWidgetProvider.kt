package com.example.glasswidgets.feature.appcontainer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.example.glasswidgets.R
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType

/**
 * Proveedor de App Widget para el indicador de batería estilo Liquid Glass.
 *
 * NOTA ARQUITECTÓNICA SOBRE CICLO DE ACTUALIZACIÓN EN ANDROID 8.0+ (API 26+):
 * A partir de Android 8.0, el sistema prohíbe el registro estático en AndroidManifest.xml
 * del evento dinámico [Intent.ACTION_BATTERY_CHANGED]. Por este motivo:
 * 1. Los eventos de conexión/desconexión de corriente ([Intent.ACTION_POWER_CONNECTED] y
 *    [Intent.ACTION_POWER_DISCONNECTED]) sí están registrados en el manifiesto y provocan
 *    una actualización instantánea.
 * 2. La lectura del nivel de batería mientras el dispositivo está desenchufado se realiza
 *    mediante inspección activa de la sticky intent [Intent.ACTION_BATTERY_CHANGED] al actualizar,
 *    complementado por la actualización periódica del sistema (30 min) y por la acción táctil directa
 *    sobre el widget (manual refresh).
 */
class BatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == Intent.ACTION_POWER_CONNECTED || action == Intent.ACTION_POWER_DISCONNECTED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, BatteryWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (action == ACTION_BATTERY_MANUAL_UPDATE) {
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (ids != null) {
                onUpdate(context, AppWidgetManager.getInstance(context), ids)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = WidgetConfigRepository(context)
        for (appWidgetId in appWidgetIds) {
            repository.deleteConfig(appWidgetId)
        }
    }

    companion object {
        const val ACTION_BATTERY_MANUAL_UPDATE = "com.example.glasswidgets.ACTION_BATTERY_MANUAL_UPDATE"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = WidgetConfigRepository(context)
            if (!repository.hasConfig(appWidgetId)) return

            val visualConfig = repository.getVisualConfig(appWidgetId)
            val layoutType = repository.getLayoutType(appWidgetId)

            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val density = context.resources.displayMetrics.density
            
            val defaultWidth = if (layoutType == WidgetLayoutType.BATTERY_4X2) 300 else 140
            val defaultHeight = if (layoutType == WidgetLayoutType.BATTERY_4X2) 140 else 70

            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: defaultWidth
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: defaultHeight
            
            val widthPx = (minWidthDp * density).toInt()
            val heightPx = (maxHeightDp * density).toInt()

            val bgBitmap = DrawableUtils.createGlassBackgroundBitmap(widthPx, heightPx, visualConfig.baseColor, visualConfig.opacity, visualConfig.cornerRadius)
            views.setImageViewBitmap(R.id.widgetBackground, bgBitmap)

            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            var level = -1
            var statusStr = "Desconocido"
            
            if (batteryIntent != null) {
                val rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (rawLevel != -1 && scale != -1) {
                    level = (rawLevel * 100 / scale.toFloat()).toInt()
                }

                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                statusStr = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
                    BatteryManager.BATTERY_STATUS_FULL -> "Completada"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descargando"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "No carga"
                    else -> "Desconocido"
                }
            }

            val displayLevel = if (level >= 0) level else 50
            
            if (visualConfig.showBatteryPercentage) {
                views.setViewVisibility(R.id.batteryPercentage, View.VISIBLE)
                views.setTextViewText(R.id.batteryPercentage, "$displayLevel%")
                views.setTextColor(R.id.batteryPercentage, visualConfig.textColor)
                val size = if (layoutType == WidgetLayoutType.BATTERY_4X2) 48f else 28f
                views.setTextViewTextSize(R.id.batteryPercentage, TypedValue.COMPLEX_UNIT_SP, size * visualConfig.clockSizeMultiplier)
            } else {
                views.setViewVisibility(R.id.batteryPercentage, View.GONE)
            }

            views.setTextViewText(R.id.batteryStatus, statusStr)
            views.setTextColor(R.id.batteryStatus, visualConfig.textColor and 0xCCFFFFFF.toInt())
            val statusSize = if (layoutType == WidgetLayoutType.BATTERY_4X2) 16f else 12f
            views.setTextViewTextSize(R.id.batteryStatus, TypedValue.COMPLEX_UNIT_SP, statusSize * visualConfig.clockSizeMultiplier)

            val barWidthPx = ((if (layoutType == WidgetLayoutType.BATTERY_4X2) 200 else 100) * density).toInt()
            val barHeightPx = (8 * density).toInt()
            val batteryBitmap = DrawableUtils.createBatteryBarBitmap(
                widthPx = barWidthPx,
                heightPx = barHeightPx,
                level = displayLevel / 100f,
                color = visualConfig.batteryIndicatorColor,
                cornerRadiusDp = 4f
            )
            views.setImageViewBitmap(R.id.batteryBar, batteryBitmap)

            val selfIntent = Intent(context, BatteryWidgetProvider::class.java).apply {
                action = ACTION_BATTERY_MANUAL_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                data = "glasswidget://battery/$appWidgetId".toUri()
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                selfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetBackground, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
