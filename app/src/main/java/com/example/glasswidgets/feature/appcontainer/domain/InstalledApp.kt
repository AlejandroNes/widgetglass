package com.example.glasswidgets.feature.appcontainer.domain

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)
