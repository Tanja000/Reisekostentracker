package de.saveiq.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import org.maplibre.android.annotations.IconFactory
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
    val isEn = currentLanguage.isEnglish
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val expensesWithLocation = remember(expenses) {
        expenses.filter { it.latitude != null && it.longitude != null }
    }

    val displayedExpenses = expensesWithLocation

    val sortedByCost = remember(displayedExpenses) {
        displayedExpenses.sortedByDescending { it.amount }
    }

    // Hotspot grouping by Location Name
    val locationHotspots = remember(expensesWithLocation, isEn) {
        expensesWithLocation
            .groupBy { it.locationName?.trim().takeUnless { s -> s.isNullOrEmpty() } ?: (if (isEn) "Unknown Location" else "Unbekannter Ort") }
            .map { (locName, list) ->
                val total = list.sumOf { it.amount }
                val count = list.size
                val firstWithCoord = list.first()
                Triple(locName, total, firstWithCoord)
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var mapLibreInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    val currencyFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val dateFormat = remember(isEn) { SimpleDateFormat(if (isEn) "MMM dd, yyyy" else "dd. MMM yyyy", if (isEn) Locale.US else Locale.GERMAN) }

    LaunchedEffect(displayedExpenses, mapLibreInstance) {
        val map = mapLibreInstance ?: return@LaunchedEffect
        map.clear()
        displayedExpenses.forEach { exp ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(exp.latitude!!, exp.longitude!!))
                    .title(exp.title)
                    .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: (if (isEn) "Location" else "Standort")}")
                    .icon(createPriceBadgeIcon(exp.amount, context))
            )
        }
        if (displayedExpenses.isNotEmpty()) {
            try {
                val boundsBuilder = LatLngBounds.Builder()
                displayedExpenses.forEach {
                    boundsBuilder.include(LatLng(it.latitude!!, it.longitude!!))
                }
                map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 40)
                )
            } catch (_: Exception) {}
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${displayedExpenses.size} ${if (isEn) "Markers" else "Marker"}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn) "Color-coded price pins instantly show affordable and expensive spots."
                    else "Farbcodierte Preis-Pins zeigen sofort, wo es besonders günstig oder teuer war.",
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

        // Map View Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                onCreate(null)
                                onStart()
                                onResume()
                                getMapAsync { mapLibreMap ->
                                    mapLibreInstance = mapLibreMap
                                    val styleUri = if (isDarkTheme) {
                                        "https://tiles.openfreemap.org/styles/positron"
                                    } else {
                                        "https://tiles.openfreemap.org/styles/bright"
                                    }
                                    mapLibreMap.setStyle(Style.Builder().fromUri(styleUri)) {
                                        mapLibreMap.clear()
                                        displayedExpenses.forEach { exp ->
                                            mapLibreMap.addMarker(
                                                MarkerOptions()
                                                    .position(LatLng(exp.latitude!!, exp.longitude!!))
                                                    .title(exp.title)
                                                    .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                                    .icon(createPriceBadgeIcon(exp.amount, context))
                                            )
                                        }
                                        if (displayedExpenses.isNotEmpty()) {
                                            try {
                                                val boundsBuilder = LatLngBounds.Builder()
                                                displayedExpenses.forEach {
                                                    boundsBuilder.include(LatLng(it.latitude!!, it.longitude!!))
                                                }
                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 40)
                                                )
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    mapLibreMap.setOnMarkerClickListener { marker ->
                                        val match = displayedExpenses.find { it.title == marker.title }
                                        selectedExpense = match
                                        mapLibreMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                marker.position,
                                                14.5
                                            )
                                        )
                                        false
                                    }
                                }
                            }
                        },
                        update = { view ->
                            mapLibreInstance?.let { map ->
                                map.clear()
                                displayedExpenses.forEach { exp ->
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(exp.latitude!!, exp.longitude!!))
                                            .title(exp.title)
                                            .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                            .icon(createPriceBadgeIcon(exp.amount, context))
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (selectedExpense != null) {
                        selectedExpense?.let { exp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val cat = categoryMap[exp.categoryId]
                                    if (cat != null) {
                                        CategoryIconBadge(category = cat, size = 38)
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = exp.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(
                                            text = "📍 ${exp.locationName ?: "Koordinaten"} • ${currencyFormat.format(exp.amount)} €",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    IconButton(
                                        onClick = { selectedExpense = null },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Discreet Attribution (OSM & OpenFreeMap)
                    Surface(
                        modifier = Modifier
                            .align(if (selectedExpense != null) Alignment.TopEnd else Alignment.BottomEnd)
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
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        if (sampleExp.latitude != null && sampleExp.longitude != null) {
                                            mapLibreInstance?.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(sampleExp.latitude, sampleExp.longitude),
                                                    13.0
                                                )
                                            )
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (total > 200.0) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant
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
                                            color = if (total > 200.0) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
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

        item {
            Text(
                text = if (isEn) "All Locations & Expense Ranking" else "Alle Orte & Ausgaben-Rangliste",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(sortedByCost) { exp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedExpense = exp
                        if (exp.latitude != null && exp.longitude != null) {
                            mapLibreInstance?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(exp.latitude, exp.longitude),
                                    14.5
                                )
                            )
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedExpense?.id == exp.id)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cat = categoryMap[exp.categoryId] ?: ExpenseCategory(
                        id = exp.categoryId,
                        name = "Sonstiges",
                        iconName = "category",
                        colorValue = 0xFF757575L
                    )
                    CategoryIconBadge(category = cat, size = 42)

                    Spacer(modifier = Modifier.width(12.dp))

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

    // Shadow / base paint
    val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = android.graphics.Paint.Style.FILL
    }

    val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isSuperExpensive) android.graphics.Color.parseColor("#FEF08A") else android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = if (isSuperExpensive) 4f else 3f
    }

    // Path for rounded pill + triangle arrow at bottom center
    val path = android.graphics.Path()
    val rect = android.graphics.RectF(2f, 2f, pillWidth - 2f, pillHeight)
    val cornerRadius = 22f
    path.addRoundRect(rect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)

    // Pointer arrow
    val centerX = pillWidth / 2f
    path.moveTo(centerX - 10f, pillHeight - 1f)
    path.lineTo(centerX, totalHeight - 2f)
    path.lineTo(centerX + 10f, pillHeight - 1f)
    path.close()

    canvas.drawPath(path, bgPaint)
    canvas.drawPath(path, strokePaint)

    // Draw text
    val textY = (pillHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(labelText, centerX, textY, textPaint)

    return org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
}
