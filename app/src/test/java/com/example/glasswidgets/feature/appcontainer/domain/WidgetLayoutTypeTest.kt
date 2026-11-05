package com.example.glasswidgets.feature.appcontainer.domain

import org.junit.Assert.*
import org.junit.Test

class WidgetLayoutTypeTest {

    @Test
    fun testLayoutCategories() {
        assertTrue(WidgetLayoutType.CLOCK_2X1.isClock)
        assertTrue(WidgetLayoutType.CLOCK_4X2.isClock)
        assertFalse(WidgetLayoutType.CLOCK_2X1.isBattery)
        assertFalse(WidgetLayoutType.CLOCK_2X1.isAppContainer)

        assertTrue(WidgetLayoutType.BATTERY_2X1.isBattery)
        assertTrue(WidgetLayoutType.BATTERY_4X2.isBattery)
        assertFalse(WidgetLayoutType.BATTERY_2X1.isClock)

        assertTrue(WidgetLayoutType.CALENDAR_2X2.isCalendar)
        assertTrue(WidgetLayoutType.CALENDAR_4X4.isCalendar)
        assertFalse(WidgetLayoutType.CALENDAR_2X2.isAppContainer)

        assertTrue(WidgetLayoutType.HORIZONTAL_3.isAppContainer)
        assertTrue(WidgetLayoutType.SQUARE_4.isAppContainer)
        assertTrue(WidgetLayoutType.SQUARE_9.isAppContainer)
        assertFalse(WidgetLayoutType.HORIZONTAL_3.isClock)
    }

    @Test
    fun testMaxAppsCapacity() {
        assertEquals(3, WidgetLayoutType.HORIZONTAL_3.maxApps)
        assertEquals(4, WidgetLayoutType.SQUARE_4.maxApps)
        assertEquals(9, WidgetLayoutType.SQUARE_9.maxApps)
        assertEquals(0, WidgetLayoutType.CLOCK_2X1.maxApps)
        assertEquals(0, WidgetLayoutType.BATTERY_4X2.maxApps)
    }

    @Test
    fun testFromStringFallback() {
        assertEquals(WidgetLayoutType.HORIZONTAL_3, WidgetLayoutType.fromString(null))
        assertEquals(WidgetLayoutType.HORIZONTAL_3, WidgetLayoutType.fromString(""))
        assertEquals(WidgetLayoutType.HORIZONTAL_3, WidgetLayoutType.fromString("INVALID_LAYOUT"))
        assertEquals(WidgetLayoutType.CLOCK_4X2, WidgetLayoutType.fromString("CLOCK_4X2"))
        assertEquals(WidgetLayoutType.CALENDAR_4X4, WidgetLayoutType.fromString("CALENDAR_4X4"))
    }
}
