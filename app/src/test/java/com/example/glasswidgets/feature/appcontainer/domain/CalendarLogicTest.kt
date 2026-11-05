package com.example.glasswidgets.feature.appcontainer.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class CalendarLogicTest {

    @Test
    fun testLeapYearFebruaryDays() {
        val leapCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val leapDays = leapCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(29, leapDays)

        val nonLeapCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val nonLeapDays = nonLeapCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(28, nonLeapDays)
    }

    @Test
    fun testMonthOffsetNavigationCrossingYearBoundary() {
        val baseCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
        }

        // Simular avanzar 2 meses
        val targetCal = (baseCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 2)
        }

        assertEquals(2027, targetCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, targetCal.get(Calendar.MONTH))

        // Simular retroceder 14 meses desde Dic 2026
        val prevCal = (baseCal.clone() as Calendar).apply {
            add(Calendar.MONTH, -14)
        }
        assertEquals(2025, prevCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, prevCal.get(Calendar.MONTH))
    }

    @Test
    fun testStartPaddingCalculation() {
        val startDayOfWeek = Calendar.WEDNESDAY // 4
        val firstDayOfUserWeek = Calendar.SUNDAY // 1

        val startPadding = (startDayOfWeek - firstDayOfUserWeek + 7) % 7
        assertEquals(3, startPadding) // Dom(0), Lun(1), Mar(2) -> 3 celdas vacías
    }
}
