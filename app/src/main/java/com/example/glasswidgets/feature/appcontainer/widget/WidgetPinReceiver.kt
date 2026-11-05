package com.example.glasswidgets.feature.appcontainer.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository

class WidgetPinReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val pendingId = intent.getStringExtra("EXTRA_PENDING_ID")

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && !pendingId.isNullOrEmpty()) {
            val repository = WidgetConfigRepository(context)
            if (repository.consumePendingConfig(pendingId, appWidgetId)) {
                val layout = repository.getLayoutType(appWidgetId)
                if (layout.isClock) {
                    ClockWidgetProvider.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                } else if (layout.isBattery) {
                    BatteryWidgetProvider.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                } else if (layout.isCalendar) {
                    CalendarWidgetProvider.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                } else {
                    AppContainerWidgetProvider.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
        }
    }
}
