package com.example.glasswidgets.feature.appcontainer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.example.glasswidgets.R
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = WidgetConfigRepository(context)
        for (appWidgetId in appWidgetIds) {
            repository.deleteConfig(appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = WidgetConfigRepository(context)
            if (!repository.hasConfig(appWidgetId)) return
            
            val visualConfig = repository.getVisualConfig(appWidgetId)
            val layoutType = repository.getLayoutType(appWidgetId)
            
            val views = RemoteViews(context.packageName, R.layout.widget_clock)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val density = context.resources.displayMetrics.density
            
            val defaultWidth = if (layoutType == WidgetLayoutType.CLOCK_4X2) 300 else 140
            val defaultHeight = if (layoutType == WidgetLayoutType.CLOCK_4X2) 140 else 70

            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: defaultWidth
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: defaultHeight
            
            val widthPx = (minWidthDp * density).toInt()
            val heightPx = (maxHeightDp * density).toInt()

            val bgBitmap = DrawableUtils.createGlassBackgroundBitmap(
                widthPx = widthPx,
                heightPx = heightPx,
                baseColor = visualConfig.baseColor,
                opacity = visualConfig.opacity,
                cornerRadiusDp = visualConfig.cornerRadius
            )
            views.setImageViewBitmap(R.id.widgetBackground, bgBitmap)
            
            val baseTimeSize = if (layoutType == WidgetLayoutType.CLOCK_4X2) 64f else 36f
            val baseDateSize = if (layoutType == WidgetLayoutType.CLOCK_4X2) 16f else 12f
            
            val timeSize = baseTimeSize * visualConfig.clockSizeMultiplier
            val dateSize = baseDateSize * visualConfig.clockSizeMultiplier

            views.setTextViewTextSize(R.id.clockTime, TypedValue.COMPLEX_UNIT_SP, timeSize)
            views.setTextColor(R.id.clockTime, visualConfig.textColor)
            
            if (visualConfig.showDate) {
                views.setViewVisibility(R.id.clockDate, View.VISIBLE)
                views.setTextViewTextSize(R.id.clockDate, TypedValue.COMPLEX_UNIT_SP, dateSize)
                views.setTextColor(R.id.clockDate, visualConfig.textColor and 0xCCFFFFFF.toInt()) 
            } else {
                views.setViewVisibility(R.id.clockDate, View.GONE)
            }

            val clockIntent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val packageManager = context.packageManager
            val resolvedActivity = clockIntent.resolveActivity(packageManager)
            if (resolvedActivity != null) {
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    clockIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.clockContainer, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
