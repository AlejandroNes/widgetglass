package com.example.glasswidgets.feature.appcontainer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetVisualConfigTest {

    @Test
    fun testDefaultConfigValues() {
        val defaultConfig = WidgetVisualConfig()
        assertEquals(0xFFFFFF, defaultConfig.baseColor)
        assertEquals(0.2f, defaultConfig.opacity, 0.01f)
        assertEquals(32f, defaultConfig.cornerRadius, 0.01f)
        assertEquals(1.0f, defaultConfig.iconScale, 0.01f)
        assertTrue(defaultConfig.showDate)
        assertTrue(defaultConfig.showBatteryPercentage)
    }

    @Test
    fun testPresetValues() {
        val clear = WidgetVisualConfig.PRESET_CLEAR
        assertEquals(0.15f, clear.opacity, 0.01f)
        assertEquals(36f, clear.cornerRadius, 0.01f)

        val frosted = WidgetVisualConfig.PRESET_FROSTED
        assertEquals(0.35f, frosted.opacity, 0.01f)

        val dark = WidgetVisualConfig.PRESET_DARK
        assertEquals(0x1A1A1A, dark.baseColor)

        val soft = WidgetVisualConfig.PRESET_SOFT
        assertEquals(0xE1F5FE, soft.baseColor)
    }
}
