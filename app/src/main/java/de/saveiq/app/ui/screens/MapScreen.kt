package de.saveiq.app.ui.screens

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import de.saveiq.app.model.Expense
import de.saveiq.app.model.ExpenseCategory
import de.saveiq.app.ui.components.CategoryIconBadge
import de.saveiq.app.ui.viewmodel.ExpenseViewModel
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MapScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isEn = currentLanguage.isEnglish
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val expensesWithLocation = remember(expenses) {
        expenses.filter { it.latitude != null && it.longitude != null }
    }

    // Multi-selection: Set of selected expense IDs
    val selectedExpenseIds = remember { mutableStateListOf<String>() }

    // Fullscreen map mode toggle
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    // MapLibre map reference
    var mapLibreInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    // Keep selected IDs clean if expenses are deleted
    LaunchedEffect(expensesWithLocation) {
        val validIds = expensesWithLocation.map { it.id }.toSet()
        selectedExpenseIds.removeAll { it !in validIds }
    }

    // Determine which expenses to show on the map:
    // If user has selected one or more specific expenses, only show those.
    // If none are specifically selected, show all expenses with location.
    val displayedExpenses = remember(expensesWithLocation, selectedExpenseIds.toList()) {
        if (selectedExpenseIds.isNotEmpty()) {
            expensesWithLocation.filter { it.id in selectedExpenseIds }
        } else {
            expensesWithLocation
        }
    }

    // Ranking sorted by amount descending
    val sortedByCost = remember(expensesWithLocation) {
        expensesWithLocation.sortedByDescending { it.amount }
    }

    // Hotspot grouping by Location Name
    val locationHotspots = remember(expensesWithLocation, isEn) {
        expensesWithLocation
            .groupBy { it.locationName?.trim().takeUnless { s -> s.isNullOrEmpty() } ?: (if (isEn) "Unknown Location" else "Unbekannter Ort") }
            .map { (locName, list) ->
                val total = list.sumOf { it.amount }
                val firstWithCoord = list.first()
                Triple(locName, total, firstWithCoord)
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    val currencyFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val dateFormat = remember(isEn) { SimpleDateFormat(if (isEn) "MMM dd, yyyy" else "dd. MMM yyyy", if (isEn) Locale.US else Locale.GERMAN) }

    // Helper: update markers and camera on the map
    fun updateMapCamera(targetExpenses: List<Expense>, animate: Boolean = true) {
        val map = mapLibreInstance ?: return
        if (targetExpenses.size == 1) {
            val single = targetExpenses.first()
            if (single.latitude != null && single.longitude != null) {
                val update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(single.latitude, single.longitude),
                    15.5
                )
                if (animate) map.animateCamera(update) else map.moveCamera(update)
            }
        } else if (targetExpenses.size > 1) {
            try {
                val boundsBuilder = LatLngBounds.Builder()
                targetExpenses.forEach {
                    if (it.latitude != null && it.longitude != null) {
                        boundsBuilder.include(LatLng(it.latitude, it.longitude))
                    }
                }
                val update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 70)
                if (animate) map.animateCamera(update) else map.moveCamera(update)
            } catch (_: Exception) {
                val first = targetExpenses.firstOrNull { it.latitude != null && it.longitude != null }
                if (first != null) {
                    val update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(first.latitude!!, first.longitude!!),
                        13.0
                    )
                    if (animate) map.animateCamera(update) else map.moveCamera(update)
                }
            }
        }
    }

    // Refresh markers & camera when displayedExpenses change
    LaunchedEffect(displayedExpenses, mapLibreInstance) {
        val map = mapLibreInstance ?: return@LaunchedEffect
        map.clear()
        displayedExpenses.forEach { exp ->
            if (exp.latitude != null && exp.longitude != null) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(exp.latitude, exp.longitude))
                        .title(exp.title)
                        .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: (if (isEn) "Location" else "Standort")}")
                        .icon(createPriceBadgeIcon(exp.amount, context))
                )
            }
        }
        updateMapCamera(displayedExpenses, animate = true)
    }

    // Android back button exits fullscreen map
    if (isFullscreen) {
        BackHandler {
            isFullscreen = false
        }
    }

    // Reusable MapView Composable
    @Composable
    fun MapContainer(isFull: Boolean, mapModifier: Modifier = Modifier) {
        Card(
            modifier = mapModifier,
            shape = if (isFull) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(if (isFull) 0.dp else 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onCreate(null)
                            onStart()
                            onResume()

                            // Fix touch interception: ensure parent LazyColumn/scroll never steals drag events
                            setOnTouchListener { view, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        view.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        view.parent?.requestDisallowInterceptTouchEvent(false)
                                    }
                                }
                                false
                            }

                            getMapAsync { mapLibreMap ->
                                mapLibreInstance = mapLibreMap

                                // Enable all gesture interactions explicitly
                                mapLibreMap.uiSettings.isScrollGesturesEnabled = true
                                mapLibreMap.uiSettings.isZoomGesturesEnabled = true
                                mapLibreMap.uiSettings.isTiltGesturesEnabled = true
                                mapLibreMap.uiSettings.isRotateGesturesEnabled = true

                                val styleUri = if (isDarkTheme) {
                                    "https://tiles.openfreemap.org/styles/positron"
                                } else {
                                    "https://tiles.openfreemap.org/styles/bright"
                                }
                                mapLibreMap.setStyle(Style.Builder().fromUri(styleUri)) {
                                    mapLibreMap.clear()
                                    displayedExpenses.forEach { exp ->
                                        if (exp.latitude != null && exp.longitude != null) {
                                            mapLibreMap.addMarker(
                                                MarkerOptions()
                                                    .position(LatLng(exp.latitude, exp.longitude))
                                                    .title(exp.title)
                                                    .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                                    .icon(createPriceBadgeIcon(exp.amount, context))
                                            )
                                        }
                                    }
                                    if (displayedExpenses.isNotEmpty()) {
                                        updateMapCamera(displayedExpenses, animate = false)
                                    }
                                }

                                mapLibreMap.setOnMarkerClickListener { marker ->
                                    val match = expensesWithLocation.find {
                                        it.title == marker.title &&
                                            it.latitude == marker.position.latitude &&
                                            it.longitude == marker.position.longitude
                                    } ?: expensesWithLocation.find { it.title == marker.title }

                                    if (match != null) {
                                        if (selectedExpenseIds.contains(match.id) && selectedExpenseIds.size == 1) {
                                            selectedExpenseIds.clear()
                                        } else {
                                            selectedExpenseIds.clear()
                                            selectedExpenseIds.add(match.id)
                                        }
                                    }
                                    false
                                }
                            }
                        }
                    },
                    update = {
                        mapLibreInstance?.let { map ->
                            map.clear()
                            displayedExpenses.forEach { exp ->
                                if (exp.latitude != null && exp.longitude != null) {
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(exp.latitude, exp.longitude))
                                            .title(exp.title)
                                            .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                            .icon(createPriceBadgeIcon(exp.amount, context))
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Controls Bar on Map: Fullscreen Toggle & Pin counter & Reset Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Marker Count Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedExpenseIds.isNotEmpty()) {
                                    if (isEn) "${selectedExpenseIds.size} of ${expensesWithLocation.size} selected"
                                    else "${selectedExpenseIds.size} von ${expensesWithLocation.size} gewählt"
                                } else {
                                    if (isEn) "All (${displayedExpenses.size}) Pins"
                                    else "Alle (${displayedExpenses.size}) Pins"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Action buttons (Reset Selection + Fullscreen Toggle)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset button if selection active
                        if (selectedExpenseIds.isNotEmpty()) {
                            FilledIconButton(
                                onClick = { selectedExpenseIds.clear() },
                                modifier = Modifier.size(38.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isEn) "Show all pins" else "Alle Pins anzeigen",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Fullscreen / Exit Fullscreen Button
                        FilledIconButton(
                            onClick = { isFullscreen = !isFullscreen },
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isFull) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFull) (if (isEn) "Exit Fullscreen" else "Vollbild beenden")
                                else (if (isEn) "Fullscreen Map" else "Vollbild-Karte"),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom banner when exactly 1 expense is selected
                if (selectedExpenseIds.size == 1) {
                    val singleSelected = expensesWithLocation.find { it.id == selectedExpenseIds.first() }
                    if (singleSelected != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cat = categoryMap[singleSelected.categoryId]
                                if (cat != null) {
                                    CategoryIconBadge(category = cat, size = 38)
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = singleSelected.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        text = "📍 ${singleSelected.locationName ?: (if (isEn) "Location" else "Standort")} • ${currencyFormat.format(singleSelected.amount)} €",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(
                                    onClick = { selectedExpenseIds.clear() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Attribution (OSM & OpenFreeMap)
                Surface(
                    modifier = Modifier
                        .align(if (selectedExpenseIds.size == 1) Alignment.TopStart else Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "© OpenStreetMap • OpenFreeMap",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    // If Fullscreen mode is active, display ONLY the map occupying 100% of the screen
    if (isFullscreen) {
        Box(modifier = modifier.fillMaxSize()) {
            MapContainer(isFull = true, mapModifier = Modifier.fillMaxSize())
        }
        return
    }

    // Normal Standard Split Layout (Map + Hotspots + Ranking with multi-selection)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Expenses Map" else "Ausgaben-Karte",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Fullscreen Quick Action Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isFullscreen = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Fullscreen" else "Vollbild",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn) "Farbcodierte Preis-Pins. Wische die Karte flüssig oder öffne den Vollbildmodus."
                    else "Farbcodierte Preis-Pins. Wische die Karte flüssig oder öffne den Vollbildmodus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price Legend Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF10B981), label = if (isEn) "< 15 € (Budget)" else "< 15 € (Günstig)")
                    LegendItem(color = Color(0xFFF59E0B), label = if (isEn) "15-50 € (Moderate)" else "15-50 € (Moderat)")
                    LegendItem(color = Color(0xFFF97316), label = if (isEn) "50-100 € (Upscale)" else "50-100 € (Gehoben)")
                    LegendItem(color = Color(0xFFDC2626), label = if (isEn) "> 100 € 🔥 (Luxury)" else "> 100 € 🔥 (Luxus)")
                }
            }
        }

        // Map View Card (Normal View: 340dp height, smoothly draggable)
        item {
            MapContainer(
                isFull = false,
                mapModifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            )
        }

        // Teuerste Städte / Hotspots Section
        if (locationHotspots.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "🏆 Top Spending Hotspots" else "🏆 Teuerste Ausgaben-Hotspots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) "By Location" else "Nach Ort",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(locationHotspots) { (name, total, sampleExp) ->
                            val isHotspotSelected = selectedExpenseIds.contains(sampleExp.id) && selectedExpenseIds.size == 1
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        if (isHotspotSelected) {
                                            selectedExpenseIds.clear()
                                        } else {
                                            selectedExpenseIds.clear()
                                            selectedExpenseIds.add(sampleExp.id)
                                            updateMapCamera(listOf(sampleExp), animate = true)
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isHotspotSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isHotspotSelected)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    else if (total > 200.0)
                                        Color(0xFFDC2626).copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = if (total > 200.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${currencyFormat.format(total)} €",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (total > 200.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Header: Ranking with Multi-Select Controls
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "All Locations & Ranking" else "Alle Orte & Ausgaben-Rangliste",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) "Click to zoom to point. Check multiple boxes to show together."
                            else "Klick zoomt zum Punkt. Mehrere per Checkbox auswählen.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Selection Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedExpenseIds.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedExpenseIds.clear() },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isEn) "Show all" else "Alle zeigen",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedExpenseIds.clear()
                                        selectedExpenseIds.addAll(expensesWithLocation.map { it.id })
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isEn) "Select all" else "Alle markieren",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // List of Expenses with Multi-Selection Checkbox & Click-to-Zoom
        items(sortedByCost) { exp ->
            val isChecked = selectedExpenseIds.contains(exp.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Clicking an entry: switch camera view to that point on the map!
                        if (isChecked && selectedExpenseIds.size == 1) {
                            selectedExpenseIds.clear()
                        } else {
                            selectedExpenseIds.clear()
                            selectedExpenseIds.add(exp.id)
                            updateMapCamera(listOf(exp), animate = true)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(
                    width = if (isChecked) 1.5.dp else 1.dp,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox for multi-selection (allows viewing multiple points together)
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (!selectedExpenseIds.contains(exp.id)) {
                                    selectedExpenseIds.add(exp.id)
                                }
                            } else {
                                selectedExpenseIds.remove(exp.id)
                            }
                            val targetList = if (selectedExpenseIds.isNotEmpty()) {
                                expensesWithLocation.filter { it.id in selectedExpenseIds }
                            } else {
                                expensesWithLocation
                            }
                            updateMapCamera(targetList, animate = true)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(36.dp)
                    )

                    val cat = categoryMap[exp.categoryId] ?: ExpenseCategory(
                        id = exp.categoryId,
                        name = "Sonstiges",
                        iconName = "category",
                        colorValue = 0xFF757575L
                    )
                    CategoryIconBadge(category = cat, size = 38)

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exp.locationName ?: (if (isEn) "Unknown Location" else "Unbekannter Ort"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exp.title} • ${dateFormat.format(Date(exp.date))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val isHighCost = exp.amount >= 100.0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isHighCost) {
                                Text("🔥", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = "${currencyFormat.format(exp.amount)} €",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    exp.amount >= 100.0 -> Color(0xFFDC2626)
                                    exp.amount >= 50.0 -> Color(0xFFEA580C)
                                    exp.amount >= 15.0 -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                }
                            )
                        }
                        if (exp.latitude != null && exp.longitude != null) {
                            Text(
                                text = String.format(Locale.US, "%.2f, %.2f", exp.latitude, exp.longitude),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun createPriceBadgeIcon(amount: Double, context: android.content.Context): org.maplibre.android.annotations.Icon {
    val amtInt = amount.toInt()
    val isSuperExpensive = amount >= 100.0
    val isExpensive = amount >= 50.0 && amount < 100.0
    val isModerate = amount >= 15.0 && amount < 50.0

    val bgColor = when {
        isSuperExpensive -> android.graphics.Color.parseColor("#DC2626") // Deep Red
        isExpensive -> android.graphics.Color.parseColor("#F97316")      // Orange
        isModerate -> android.graphics.Color.parseColor("#D97706")       // Amber
        else -> android.graphics.Color.parseColor("#059669")             // Emerald Green
    }

    val labelText = if (isSuperExpensive) "🔥 $amtInt €" else "$amtInt €"

    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)

    val horizPadding = 24f
    val pillWidth = (textBounds.width() + horizPadding * 2).coerceAtLeast(80f)
    val pillHeight = 44f
    val pointerHeight = 12f
    val totalHeight = pillHeight + pointerHeight

    val bitmap = android.graphics.Bitmap.createBitmap(pillWidth.toInt(), totalHeight.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = android.graphics.Paint.Style.FILL
    }

    val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isSuperExpensive) android.graphics.Color.parseColor("#FEF08A") else android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = if (isSuperExpensive) 4f else 3f
    }

    val path = android.graphics.Path()
    val rect = android.graphics.RectF(2f, 2f, pillWidth - 2f, pillHeight)
    val cornerRadius = 22f
    path.addRoundRect(rect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)

    val centerX = pillWidth / 2f
    path.moveTo(centerX - 10f, pillHeight - 1f)
    path.lineTo(centerX, totalHeight - 2f)
    path.lineTo(centerX + 10f, pillHeight - 1f)
    path.close()

    canvas.drawPath(path, bgPaint)
    canvas.drawPath(path, strokePaint)

    val textY = (pillHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(labelText, centerX, textY, textPaint)

    return org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
}
