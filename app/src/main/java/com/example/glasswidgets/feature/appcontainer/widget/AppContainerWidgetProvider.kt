package com.example.glasswidgets.feature.appcontainer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.glasswidgets.R
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.ui.AppContainerConfigActivity

class AppContainerWidgetProvider : AppWidgetProvider() {

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
            
            val selectedApps = repository.getSelectedApps(appWidgetId)
            val visualConfig = repository.getVisualConfig(appWidgetId)
            val layoutType = repository.getLayoutType(appWidgetId)
            
            val views = RemoteViews(context.packageName, R.layout.widget_app_container)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val density = context.resources.displayMetrics.density
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: if (layoutType == WidgetLayoutType.HORIZONTAL_3) 250 else 200
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: if (layoutType == WidgetLayoutType.HORIZONTAL_3) 80 else 200
            
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
            
            when (layoutType) {
                WidgetLayoutType.HORIZONTAL_3 -> {
                    views.setViewVisibility(R.id.row1, View.VISIBLE)
                    views.setViewVisibility(R.id.row2, View.GONE)
                    views.setViewVisibility(R.id.row3, View.GONE)
                    views.setViewVisibility(R.id.appIcon2, View.VISIBLE) 
                }
                WidgetLayoutType.SQUARE_4 -> {
                    views.setViewVisibility(R.id.row1, View.VISIBLE)
                    views.setViewVisibility(R.id.row2, View.VISIBLE)
                    views.setViewVisibility(R.id.row3, View.GONE)
                    views.setViewVisibility(R.id.appIcon2, View.GONE) 
                    views.setViewVisibility(R.id.appIcon5, View.GONE)
                }
                WidgetLayoutType.SQUARE_9 -> {
                    views.setViewVisibility(R.id.row1, View.VISIBLE)
                    views.setViewVisibility(R.id.row2, View.VISIBLE)
                    views.setViewVisibility(R.id.row3, View.VISIBLE)
                    views.setViewVisibility(R.id.appIcon2, View.VISIBLE)
                    views.setViewVisibility(R.id.appIcon5, View.VISIBLE)
                }
                else -> {}
            }

            val packageManager = context.packageManager
            val allIconViewIds = intArrayOf(
                R.id.appIcon0, R.id.appIcon1, R.id.appIcon2,
                R.id.appIcon3, R.id.appIcon4, R.id.appIcon5,
                R.id.appIcon6, R.id.appIcon7, R.id.appIcon8
            )
            
            val activeIconIds = when (layoutType) {
                WidgetLayoutType.HORIZONTAL_3 -> listOf(R.id.appIcon0, R.id.appIcon1, R.id.appIcon2)
                WidgetLayoutType.SQUARE_4 -> listOf(R.id.appIcon0, R.id.appIcon1, R.id.appIcon3, R.id.appIcon4)
                WidgetLayoutType.SQUARE_9 -> allIconViewIds.toList()
                else -> emptyList()
            }

            for ((_, viewId) in allIconViewIds.withIndex()) {
                if (viewId in activeIconIds && activeIconIds.indexOf(viewId) < selectedApps.size) {
                    val appIndex = activeIconIds.indexOf(viewId)
                    val packageName = selectedApps[appIndex]
                    try {
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            val iconDrawable = packageManager.getApplicationIcon(packageName)
                            val bitmap = DrawableUtils.toBitmap(
                                drawable = iconDrawable, 
                                scale = visualConfig.iconScale
                            )
                            views.setImageViewBitmap(viewId, bitmap)
                            
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            val pendingIntent = PendingIntent.getActivity(
                                context,
                                appWidgetId * 100 + appIndex,
                                launchIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(viewId, pendingIntent)
                        } else {
                            // Aplicación desinstalada o deshabilitada: Mostrar icono por defecto atenuado
                            val defaultDrawable = packageManager.defaultActivityIcon
                            val bitmap = DrawableUtils.toBitmap(
                                drawable = defaultDrawable,
                                scale = visualConfig.iconScale * 0.8f
                            )
                            views.setImageViewBitmap(viewId, bitmap)

                            // Al tocar el slot no disponible, abre el editor para facilitar la sustitución
                            val editIntent = Intent(context, AppContainerConfigActivity::class.java).apply {
                                putExtra("EXTRA_IS_EDIT_MODE", true)
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            val pendingEdit = PendingIntent.getActivity(
                                context,
                                appWidgetId * 100 + appIndex,
                                editIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(viewId, pendingEdit)
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Aplicación desinstalada: representación visual clara y enlace al editor
                        val defaultDrawable = packageManager.defaultActivityIcon
                        val bitmap = DrawableUtils.toBitmap(
                            drawable = defaultDrawable,
                            scale = visualConfig.iconScale * 0.8f
                        )
                        views.setImageViewBitmap(viewId, bitmap)

                        val editIntent = Intent(context, AppContainerConfigActivity::class.java).apply {
                            putExtra("EXTRA_IS_EDIT_MODE", true)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingEdit = PendingIntent.getActivity(
                            context,
                            appWidgetId * 100 + appIndex,
                            editIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(viewId, pendingEdit)
                    } catch (e: Exception) {
                        views.setImageViewBitmap(viewId, null)
                        views.setOnClickPendingIntent(viewId, null)
                    }
                } else {
                    views.setImageViewBitmap(viewId, null)
                    views.setOnClickPendingIntent(viewId, null)
                }
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
