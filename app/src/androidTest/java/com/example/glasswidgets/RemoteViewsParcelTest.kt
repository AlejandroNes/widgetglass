package com.example.glasswidgets

import android.os.Parcel
import android.widget.RemoteViews
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.glasswidgets.core.util.DrawableUtils
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteViewsParcelTest {

    private fun getParcelSize(remoteViews: RemoteViews): Int {
        val parcel = Parcel.obtain()
        return try {
            remoteViews.writeToParcel(parcel, 0)
            parcel.dataSize()
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun testAllWidgetRemoteViewsParcelSizesAreUnderBinderLimit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = WidgetConfigRepository(context)

        val testWidgetId = 9999
        val config = WidgetVisualConfig.PRESET_FROSTED

        // 1. App Container 3x1
        repository.saveLayoutType(testWidgetId, WidgetLayoutType.HORIZONTAL_3)
        repository.saveVisualConfig(testWidgetId, config)
        repository.saveSelectedApps(testWidgetId, listOf("com.android.chrome", "com.google.android.calendar"))

        val containerViews = RemoteViews(context.packageName, R.layout.widget_app_container)
        val bg1 = DrawableUtils.createGlassBackgroundBitmap(800, 250, config.baseColor, config.opacity, config.cornerRadius)
        containerViews.setImageViewBitmap(R.id.widgetBackground, bg1)
        val size1 = getParcelSize(containerViews)

        // 2. Clock 4x2
        repository.saveLayoutType(testWidgetId, WidgetLayoutType.CLOCK_4X2)
        val clockViews = RemoteViews(context.packageName, R.layout.widget_clock)
        val bg2 = DrawableUtils.createGlassBackgroundBitmap(900, 420, config.baseColor, config.opacity, config.cornerRadius)
        clockViews.setImageViewBitmap(R.id.widgetBackground, bg2)
        val size2 = getParcelSize(clockViews)

        // 3. Battery 4x2
        repository.saveLayoutType(testWidgetId, WidgetLayoutType.BATTERY_4X2)
        val batteryViews = RemoteViews(context.packageName, R.layout.widget_battery)
        val bg3 = DrawableUtils.createGlassBackgroundBitmap(900, 420, config.baseColor, config.opacity, config.cornerRadius)
        val bar3 = DrawableUtils.createBatteryBarBitmap(400, 20, 0.85f, config.batteryIndicatorColor, 4f)
        batteryViews.setImageViewBitmap(R.id.widgetBackground, bg3)
        batteryViews.setImageViewBitmap(R.id.batteryBar, bar3)
        val size3 = getParcelSize(batteryViews)

        // 4. Calendar 4x4
        repository.saveLayoutType(testWidgetId, WidgetLayoutType.CALENDAR_4X4)
        val calendarViews = RemoteViews(context.packageName, R.layout.widget_calendar_month)
        val bg4 = DrawableUtils.createGlassBackgroundBitmap(840, 840, config.baseColor, config.opacity, config.cornerRadius)
        calendarViews.setImageViewBitmap(R.id.widgetBackground, bg4)
        val size4 = getParcelSize(calendarViews)

        // Limpieza
        repository.deleteConfig(testWidgetId)

        // BINDER LIMIT CHECK: Each payload MUST be well under 1,000,000 bytes (1 MB)
        assertTrue("Container 3x1 payload too large: $size1 B", size1 < 500_000)
        assertTrue("Clock 4x2 payload too large: $size2 B", size2 < 500_000)
        assertTrue("Battery 4x2 payload too large: $size3 B", size3 < 600_000)
        assertTrue("Calendar 4x4 payload too large: $size4 B", size4 < 900_000)
    }
}
