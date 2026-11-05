package com.example.glasswidgets.feature.appcontainer.domain

enum class WidgetLayoutType(val maxApps: Int, val title: String, val category: String) {
    HORIZONTAL_3(3, "Contenedor 3x1", "Aplicaciones"),
    SQUARE_4(4, "Contenedor 2x2", "Aplicaciones"),
    SQUARE_9(9, "Contenedor 3x3", "Aplicaciones"),
    CLOCK_2X1(0, "Reloj Compacto", "Relojes"),
    CLOCK_4X2(0, "Reloj Amplio", "Relojes"),
    BATTERY_2X1(0, "Batería Compacta", "Batería"),
    BATTERY_4X2(0, "Batería Amplia", "Batería"),
    CALENDAR_2X2(0, "Calendario Compacto", "Calendarios"),
    CALENDAR_4X4(0, "Calendario Mensual", "Calendarios");

    val isClock: Boolean get() = this == CLOCK_2X1 || this == CLOCK_4X2
    val isBattery: Boolean get() = this == BATTERY_2X1 || this == BATTERY_4X2
    val isCalendar: Boolean get() = this == CALENDAR_2X2 || this == CALENDAR_4X4
    val isAppContainer: Boolean get() = !isClock && !isBattery && !isCalendar

    companion object {
        fun fromString(value: String?): WidgetLayoutType {
            return entries.find { it.name == value } ?: HORIZONTAL_3
        }
    }
}
