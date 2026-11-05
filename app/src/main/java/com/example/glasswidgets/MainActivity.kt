package com.example.glasswidgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.glasswidgets.feature.appcontainer.data.AppRepository
import com.example.glasswidgets.feature.appcontainer.data.WidgetConfigRepository
import com.example.glasswidgets.feature.appcontainer.domain.InstalledApp
import com.example.glasswidgets.feature.appcontainer.domain.WidgetLayoutType
import com.example.glasswidgets.feature.appcontainer.ui.AppContainerConfigActivity
import com.example.glasswidgets.feature.appcontainer.widget.AppContainerWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.BatteryWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.CalendarWidgetProvider
import com.example.glasswidgets.feature.appcontainer.widget.ClockWidgetProvider
import com.example.glasswidgets.ui.components.GlassCard
import com.example.glasswidgets.ui.components.WidgetPreviewThumbnail
import com.example.glasswidgets.ui.theme.GlassWidgetsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlassWidgetsTheme {
                MainScreenContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent() {
    val context = LocalContext.current
    val configRepo = remember { WidgetConfigRepository(context) }
    val appRepo = remember { AppRepository(context) }
    
    var widgetIds by remember { mutableStateOf(emptyList<Int>()) }
    var installedApps by remember { mutableStateOf(emptyList<InstalledApp>()) }

    LaunchedEffect(Unit) {
        installedApps = appRepo.getInstalledApps()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val providerContainer = ComponentName(context, AppContainerWidgetProvider::class.java)
                val providerClock = ComponentName(context, ClockWidgetProvider::class.java)
                val providerBattery = ComponentName(context, BatteryWidgetProvider::class.java)
                val providerCalendar = ComponentName(context, CalendarWidgetProvider::class.java)
                
                val activeSystemIds = appWidgetManager.getAppWidgetIds(providerContainer).toList() + 
                                      appWidgetManager.getAppWidgetIds(providerClock).toList() +
                                      appWidgetManager.getAppWidgetIds(providerBattery).toList() +
                                      appWidgetManager.getAppWidgetIds(providerCalendar).toList()
                widgetIds = configRepo.syncActiveWidgets(activeSystemIds)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "Glass Widgets",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Superficies Liquid Glass para tu pantalla de inicio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- SECCIÓN: EXPLORAR CATÁLOGO ---
            Text(
                text = "Explorar catálogo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val categories = listOf("Aplicaciones", "Relojes", "Batería", "Calendarios")
            var selectedCategory by remember { mutableStateOf("Aplicaciones") }

            // Filter Chips por Categoría en LazyRow para evitar cortes de palabra
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animación suave de transición entre categorías
            Crossfade(
                targetState = selectedCategory,
                label = "CatalogCategoryTransition"
            ) { category ->
                val filteredLayouts = WidgetLayoutType.entries.filter { layout ->
                    when (category) {
                        "Aplicaciones" -> layout.isAppContainer
                        "Relojes" -> layout.isClock
                        "Batería" -> layout.isBattery
                        "Calendarios" -> layout.isCalendar
                        else -> true
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredLayouts) { layout ->
                        GlassCard(
                            modifier = Modifier.width(175.dp),
                            onClick = {
                                val intent = Intent(context, AppContainerConfigActivity::class.java).apply {
                                    putExtra("EXTRA_LAYOUT_TYPE", layout.name)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                WidgetPreviewThumbnail(layoutType = layout)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = layout.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "+ Personalizar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN: MIS WIDGETS ACTIVOS ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Mis Widgets Activos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${widgetIds.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (widgetIds.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no tienes widgets en tu escritorio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Elige cualquier diseño del catálogo superior para personalizar y fijar tu primer Liquid Glass widget en tu pantalla de inicio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(widgetIds) { id ->
                        val layout = configRepo.getLayoutType(id)
                        val visualConfig = remember(id) { configRepo.getVisualConfig(id) }
                        val selectedPkgs = remember(id) { configRepo.getSelectedApps(id) }

                        GlassCard(
                            modifier = Modifier.width(170.dp),
                            onClick = {
                                val intent = Intent(context, AppContainerConfigActivity::class.java).apply {
                                    putExtra("EXTRA_IS_EDIT_MODE", true)
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                WidgetPreviewThumbnail(
                                    layoutType = layout,
                                    config = visualConfig,
                                    selectedPackages = selectedPkgs,
                                    installedApps = installedApps,
                                    modifier = Modifier.height(85.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = layout.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Editar",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
