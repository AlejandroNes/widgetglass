package com.example.glasswidgets.feature.appcontainer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.example.glasswidgets.R
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarWidgetProvider : AppWidgetProvider() {

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
        
        if (action == ACTION_NEXT_MONTH || action == ACTION_PREV_MONTH || action == ACTION_TODAY) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val repo = WidgetConfigRepository(context)
                var offset = repo.getMonthOffset(appWidgetId)
                
                when (action) {
                    ACTION_NEXT_MONTH -> offset++
                    ACTION_PREV_MONTH -> offset--
                    ACTION_TODAY -> offset = 0
                }
                
                repo.saveMonthOffset(appWidgetId, offset)
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        } else if (action == Intent.ACTION_DATE_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, CalendarWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
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
        const val ACTION_NEXT_MONTH = "com.example.glasswidgets.ACTION_NEXT_MONTH"
        const val ACTION_PREV_MONTH = "com.example.glasswidgets.ACTION_PREV_MONTH"
        const val ACTION_TODAY = "com.example.glasswidgets.ACTION_TODAY"

        private fun getPendingIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = "glasswidget://calendar/$appWidgetId/$action".toUri()
            }
            return PendingIntent.getBroadcast(
                context, 
                appWidgetId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = WidgetConfigRepository(context)
            if (!repository.hasConfig(appWidgetId)) return

            val visualConfig = repository.getVisualConfig(appWidgetId)
            val layoutType = repository.getLayoutType(appWidgetId)
            val monthOffset = repository.getMonthOffset(appWidgetId)

            val layoutRes = if (layoutType == WidgetLayoutType.CALENDAR_4X4) R.layout.widget_calendar_month else R.layout.widget_calendar_compact
            val views = RemoteViews(context.packageName, layoutRes)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val density = context.resources.displayMetrics.density
            val defaultWidth = if (layoutType == WidgetLayoutType.CALENDAR_4X4) 280 else 140
            val defaultHeight = if (layoutType == WidgetLayoutType.CALENDAR_4X4) 280 else 140
            
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: defaultWidth
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: defaultHeight

            val widthPx = (minWidthDp * density).toInt()
            val heightPx = (maxHeightDp * density).toInt()

            val bgBitmap = DrawableUtils.createGlassBackgroundBitmap(widthPx, heightPx, visualConfig.baseColor, visualConfig.opacity, visualConfig.cornerRadius)
            views.setImageViewBitmap(R.id.widgetBackground, bgBitmap)

            val today = Calendar.getInstance()
            val currentDay = today.get(Calendar.DAY_OF_MONTH)

            if (layoutType == WidgetLayoutType.CALENDAR_2X2) {
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                val dayStr = dayNameFormat.format(today.time)
                
                views.setTextViewText(R.id.compactMonth, monthFormat.format(today.time).uppercase())
                views.setTextColor(R.id.compactMonth, visualConfig.accentColor)
                
                views.setTextViewText(R.id.compactDayNumber, currentDay.toString())
                views.setTextColor(R.id.compactDayNumber, visualConfig.textColor)
                
                views.setTextViewText(R.id.compactDayName, dayStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                views.setTextColor(R.id.compactDayName, visualConfig.textColor)
                
                views.setOnClickPendingIntent(R.id.calendarContainer, getPendingIntent(context, ACTION_TODAY, appWidgetId))
            } else {
                val targetMonth = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
                val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                val formattedStr = monthYearFormat.format(targetMonth.time)
                val capitalizedStr = formattedStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                views.setTextViewText(R.id.monthYearText, capitalizedStr)
                views.setTextColor(R.id.monthYearText, visualConfig.textColor)

                views.setOnClickPendingIntent(R.id.btnPrev, getPendingIntent(context, ACTION_PREV_MONTH, appWidgetId))
                views.setOnClickPendingIntent(R.id.btnNext, getPendingIntent(context, ACTION_NEXT_MONTH, appWidgetId))
                views.setOnClickPendingIntent(R.id.btnToday, getPendingIntent(context, ACTION_TODAY, appWidgetId))
                
                views.setTextColor(R.id.btnPrev, visualConfig.textColor)
                views.setTextColor(R.id.btnNext, visualConfig.textColor)
                views.setTextColor(R.id.btnToday, visualConfig.accentColor)

                views.removeAllViews(R.id.calendarGrid)
                val firstDayOfUserWeek = targetMonth.firstDayOfWeek
                
                val headerRow = RemoteViews(context.packageName, R.layout.widget_calendar_row)
                val dayFormat = SimpleDateFormat("EE", Locale.getDefault())
                val weekCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfUserWeek) }
                
                for (_idx in 0..6) {
                    val cell = RemoteViews(context.packageName, R.layout.widget_calendar_cell)
                    cell.setTextViewText(R.id.dayText, dayFormat.format(weekCal.time).take(1).uppercase())
                    cell.setTextColor(R.id.dayText, visualConfig.accentColor)
                    headerRow.addView(R.id.rowContainer, cell)
                    weekCal.add(Calendar.DAY_OF_WEEK, 1)
                }
                views.addView(R.id.calendarGrid, headerRow)

                val daysInMonth = targetMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                targetMonth.set(Calendar.DAY_OF_MONTH, 1)
                val startDayOfWeek = targetMonth.get(Calendar.DAY_OF_WEEK)
                
                var startPadding = startDayOfWeek - firstDayOfUserWeek
                if (startPadding < 0) startPadding += 7

                var dayOfMonth = 1
                var cellCount = 0
                val isCurrentMonth = monthOffset == 0

                for (_row in 0..5) {
                    val weekRow = RemoteViews(context.packageName, R.layout.widget_calendar_row)
                    for (_col in 0..6) {
                        val cell = RemoteViews(context.packageName, R.layout.widget_calendar_cell)
                        if (cellCount >= startPadding && dayOfMonth <= daysInMonth) {
                            cell.setTextViewText(R.id.dayText, dayOfMonth.toString())
                            
                            if (isCurrentMonth && dayOfMonth == currentDay) {
                                cell.setTextColor(R.id.dayText, visualConfig.accentColor)
                            } else {
                                cell.setTextColor(R.id.dayText, visualConfig.textColor)
                            }
                            dayOfMonth++
                        } else {
                            cell.setTextViewText(R.id.dayText, "")
                        }
                        weekRow.addView(R.id.rowContainer, cell)
                        cellCount++
                    }
                    views.addView(R.id.calendarGrid, weekRow)
                    if (dayOfMonth > daysInMonth) break
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
