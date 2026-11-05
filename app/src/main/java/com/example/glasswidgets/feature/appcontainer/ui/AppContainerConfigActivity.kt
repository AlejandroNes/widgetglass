package com.example.glasswidgets.feature.appcontainer.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.glasswidgets.feature.appcontainer.data.AppRepository
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.InstalledApp
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.domain.WidgetVisualConfig
import com.example.glasswidgets.feature.appcontainer.ui.components.*
import com.example.glasswidgets.feature.appcontainer.widget.AppContainerWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.BatteryWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.CalendarWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.ClockWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.WidgetPinReceiver
import com.example.glasswidgets.ui.theme.GlassWidgetsTheme
import kotlinx.coroutines.launch
import java.util.UUID

class AppContainerConfigViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private val configRepository = WidgetConfigRepository(application)
    
    var installedApps by mutableStateOf<List<InstalledApp>>(emptyList())
        private set
        
    var selectedPackageNames by mutableStateOf<List<String>>(emptyList())
        private set
        
    var visualConfig by mutableStateOf(WidgetVisualConfig.PRESET_CLEAR)
        private set

    var layoutType by mutableStateOf(WidgetLayoutType.HORIZONTAL_3)
        private set

    var batteryLevel by mutableStateOf(85)
        private set

    var batteryStatus by mutableStateOf("Cargando")
        private set
        
    init {
        viewModelScope.launch {
            installedApps = appRepository.getInstalledApps()
        }
        
        // Cargar nivel real de batería para vistas previas de batería
        try {
            val filter = android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = application.registerReceiver(null, filter)
            if (batteryIntent != null) {
                val rawLevel = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                if (rawLevel != -1 && scale != -1) {
                    batteryLevel = (rawLevel * 100 / scale.toFloat()).toInt()
                }
                val status = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                batteryStatus = when (status) {
                    android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
                    android.os.BatteryManager.BATTERY_STATUS_FULL -> "Completada"
                    android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descargando"
                    android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "No carga"
                    else -> "Desconocido"
                }
            }
        } catch (e: Exception) {
            // Failsafe
        }
    }

    fun setLayoutPreference(newLayout: WidgetLayoutType) {
        layoutType = newLayout
        if (selectedPackageNames.size > newLayout.maxApps) {
            selectedPackageNames = selectedPackageNames.take(newLayout.maxApps)
        }
    }

    fun loadExistingConfig(widgetId: Int) {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val apps = configRepository.getSelectedApps(widgetId)
            if (apps.isNotEmpty()) {
                selectedPackageNames = apps
            }
            visualConfig = configRepository.getVisualConfig(widgetId)
            layoutType = configRepository.getLayoutType(widgetId)
        }
    }
    
    fun toggleAppSelection(packageName: String) {
        val current = selectedPackageNames.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            if (current.size < layoutType.maxApps) {
                current.add(packageName)
            }
        }
        selectedPackageNames = current
    }

    fun moveApp(from: Int, to: Int) {
        val current = selectedPackageNames.toMutableList()
        val item = current.removeAt(from)
        current.add(to, item)
        selectedPackageNames = current
    }

    fun applyPreset(preset: WidgetVisualConfig) {
        visualConfig = preset
    }

    fun updateOpacity(opacity: Float) {
        visualConfig = visualConfig.copy(opacity = opacity)
    }

    fun updateCornerRadius(radius: Float) {
        visualConfig = visualConfig.copy(cornerRadius = radius)
    }
    
    fun updateIconScale(scale: Float) {
        visualConfig = visualConfig.copy(iconScale = scale)
    }
    
    fun updateShowDate(show: Boolean) {
        visualConfig = visualConfig.copy(showDate = show)
    }

    fun updateClockSize(size: Float) {
        visualConfig = visualConfig.copy(clockSizeMultiplier = size)
    }

    fun updateShowBatteryPercentage(show: Boolean) {
        visualConfig = visualConfig.copy(showBatteryPercentage = show)
    }

    fun updateTextColor(color: Int) {
        visualConfig = visualConfig.copy(textColor = color)
    }

    fun updateBatteryColor(color: Int) {
        visualConfig = visualConfig.copy(batteryIndicatorColor = color)
    }

    fun updateAccentColor(color: Int) {
        visualConfig = visualConfig.copy(accentColor = color)
    }
    
    fun saveConfig(widgetId: Int) {
        configRepository.saveSelectedApps(widgetId, selectedPackageNames)
        configRepository.saveVisualConfig(widgetId, visualConfig)
        configRepository.saveLayoutType(widgetId, layoutType)
    }

    fun consumePendingConfig(pendingId: String, targetWidgetId: Int): Boolean = configRepository.consumePendingConfig(pendingId, targetWidgetId)
    fun hasConfig(widgetId: Int): Boolean = configRepository.hasConfig(widgetId)
    fun saveAsPending(pendingId: String) {
        configRepository.savePendingConfig(pendingId, selectedPackageNames, visualConfig, layoutType)
    }
}

class AppContainerConfigActivity : ComponentActivity() {

    private val viewModel: AppContainerConfigViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(RESULT_CANCELED)
        
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val requestedLayoutStr = intent.getStringExtra("EXTRA_LAYOUT_TYPE")
        
        if (requestedLayoutStr != null) {
            viewModel.setLayoutPreference(WidgetLayoutType.fromString(requestedLayoutStr))
        }

        val isConfigureAction = intent.action == AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        
        if (isConfigureAction && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val pendingIdFromOptions = options?.getString("glass_pending_config_id")

            if (pendingIdFromOptions != null) {
                if (viewModel.consumePendingConfig(pendingIdFromOptions, appWidgetId)) {
                    updateWidgetById(appWidgetId)
                }
                val resultValue = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
                setResult(RESULT_OK, resultValue)
                finish()
                return
            }
            
            if (viewModel.hasConfig(appWidgetId)) {
                val resultValue = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
                setResult(RESULT_OK, resultValue)
                finish()
                return
            }
        }

        if (isConfigureAction && appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val isEditMode = intent.getBooleanExtra("EXTRA_IS_EDIT_MODE", false)
        val isCreateMode = !isConfigureAction && !isEditMode

        if (isEditMode) {
            viewModel.loadExistingConfig(appWidgetId)
        }

        val buttonText = if (isCreateMode) "Agregar a pantalla de inicio" else "Guardar cambios"
        val titleText = if (isCreateMode) "Personalizar ${viewModel.layoutType.title}" else "Editar ${viewModel.layoutType.title}"

        enableEdgeToEdge()
        setContent {
            GlassWidgetsTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text(titleText, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                TextButton(onClick = { finish() }) {
                                    Text("← Atrás", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Surface(
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isCreateMode) {
                                            val appWidgetManager = AppWidgetManager.getInstance(this@AppContainerConfigActivity)
                                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                                val pendingId = UUID.randomUUID().toString()
                                                viewModel.saveAsPending(pendingId)
                                                
                                                val provider = when {
                                                    viewModel.layoutType.isClock -> ComponentName(this@AppContainerConfigActivity, ClockWidgetProvider::class.java)
                                                    viewModel.layoutType.isBattery -> ComponentName(this@AppContainerConfigActivity, BatteryWidgetProvider::class.java)
                                                    viewModel.layoutType.isCalendar -> ComponentName(this@AppContainerConfigActivity, CalendarWidgetProvider::class.java)
                                                    else -> ComponentName(this@AppContainerConfigActivity, AppContainerWidgetProvider::class.java)
                                                }
                                                
                                                val extras = Bundle().apply {
                                                    putString("glass_pending_config_id", pendingId)
                                                }
                                                
                                                val callbackIntent = Intent(this@AppContainerConfigActivity, WidgetPinReceiver::class.java).apply {
                                                    putExtra("EXTRA_PENDING_ID", pendingId)
                                                }
                                                
                                                val successCallback = PendingIntent.getBroadcast(
                                                    this@AppContainerConfigActivity,
                                                    pendingId.hashCode(),
                                                    callbackIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                                )
                                                
                                                appWidgetManager.requestPinAppWidget(provider, extras, successCallback)
                                                finish() 
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Tu launcher no admite fijar widgets directamente. Usa el menú de widgets del sistema.")
                                                }
                                            }
                                        } else {
                                            viewModel.saveConfig(appWidgetId)
                                            updateWidgetById(appWidgetId)
                                            val resultValue = Intent().apply {
                                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                            }
                                            setResult(RESULT_OK, resultValue)
                                            finish()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. HERO PREVIEW CARD
                        EditorPreviewCard(
                            config = viewModel.visualConfig,
                            layoutType = viewModel.layoutType,
                            selectedPackages = viewModel.selectedPackageNames,
                            installedApps = viewModel.installedApps,
                            batteryLevel = viewModel.batteryLevel,
                            batteryStatus = viewModel.batteryStatus
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. GLASS PRESETS SELECTOR (VISUAL CARDS)
                        GlassPresetSelector(
                            selectedConfig = viewModel.visualConfig,
                            onPresetSelected = viewModel::applyPreset
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. BASE VISUAL CONTROLS
                        BaseVisualControls(
                            config = viewModel.visualConfig,
                            onOpacityChanged = viewModel::updateOpacity,
                            onRadiusChanged = viewModel::updateCornerRadius
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. SPECIFIC WIDGET CONTROLS
                        when {
                            viewModel.layoutType.isClock -> {
                                ClockEditorSection(
                                    config = viewModel.visualConfig,
                                    onClockSizeChanged = viewModel::updateClockSize,
                                    onShowDateChanged = viewModel::updateShowDate,
                                    onTextColorSelected = viewModel::updateTextColor
                                )
                            }
                            viewModel.layoutType.isBattery -> {
                                BatteryEditorSection(
                                    config = viewModel.visualConfig,
                                    onSizeChanged = viewModel::updateClockSize,
                                    onShowPercentageChanged = viewModel::updateShowBatteryPercentage,
                                    onIndicatorColorSelected = viewModel::updateBatteryColor,
                                    onTextColorSelected = viewModel::updateTextColor
                                )
                            }
                            viewModel.layoutType.isCalendar -> {
                                CalendarEditorSection(
                                    config = viewModel.visualConfig,
                                    onSizeChanged = viewModel::updateClockSize,
                                    onAccentColorSelected = viewModel::updateAccentColor,
                                    onTextColorSelected = viewModel::updateTextColor
                                )
                            }
                            else -> {
                                AppContainerEditorSection(
                                    config = viewModel.visualConfig,
                                    layoutType = viewModel.layoutType,
                                    selectedPackages = viewModel.selectedPackageNames,
                                    installedApps = viewModel.installedApps,
                                    onScaleChanged = viewModel::updateIconScale,
                                    onMoveApp = viewModel::moveApp,
                                    onToggleAppSelection = viewModel::toggleAppSelection
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    private fun updateWidgetById(id: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        when {
            viewModel.layoutType.isClock -> ClockWidgetProvider.updateAppWidget(this, appWidgetManager, id)
            viewModel.layoutType.isBattery -> BatteryWidgetProvider.updateAppWidget(this, appWidgetManager, id)
            viewModel.layoutType.isCalendar -> CalendarWidgetProvider.updateAppWidget(this, appWidgetManager, id)
            else -> AppContainerWidgetProvider.updateAppWidget(this, appWidgetManager, id)
        }
    }
}
