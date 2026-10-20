package com.example.reisekostentracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.reisekostentracker.model.Expense
import com.example.reisekostentracker.model.ExpenseCategory
import com.example.reisekostentracker.ui.components.CategoryIconBadge
import com.example.reisekostentracker.ui.viewmodel.ExpenseViewModel
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
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val expensesWithLocation = remember(expenses) {
        expenses.filter { it.latitude != null && it.longitude != null }
    }
    val sortedByCost = remember(expensesWithLocation) {
        expensesWithLocation.sortedByDescending { it.amount }
    }

    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var mapLibreInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    val currencyFormat = remember {
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val dateFormat = remember { SimpleDateFormat("dd. MMM yyyy", Locale.GERMAN) }

    LaunchedEffect(expensesWithLocation, mapLibreInstance) {
        val map = mapLibreInstance ?: return@LaunchedEffect
        map.clear()
        expensesWithLocation.forEach { exp ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(exp.latitude!!, exp.longitude!!))
                    .title(exp.title)
                    .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                    .icon(createRedCircleIcon(exp.amount, context))
            )
        }
        if (expensesWithLocation.isNotEmpty()) {
            try {
                val boundsBuilder = LatLngBounds.Builder()
                expensesWithLocation.forEach {
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
                        text = "Reisekarte & GPS-Orte",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${expensesWithLocation.size} Orte",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Erfahre auf der Karte, wo du überall warst und wo es wie teuer war.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
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
                                        expensesWithLocation.forEach { exp ->
                                            mapLibreMap.addMarker(
                                                MarkerOptions()
                                                    .position(LatLng(exp.latitude!!, exp.longitude!!))
                                                    .title(exp.title)
                                                    .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                                    .icon(createRedCircleIcon(exp.amount, context))
                                            )
                                        }
                                        if (expensesWithLocation.isNotEmpty()) {
                                            try {
                                                val boundsBuilder = LatLngBounds.Builder()
                                                expensesWithLocation.forEach {
                                                    boundsBuilder.include(LatLng(it.latitude!!, it.longitude!!))
                                                }
                                                mapLibreMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 40)
                                                )
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                    mapLibreMap.setOnMarkerClickListener { marker ->
                                        val match = expensesWithLocation.find { it.title == marker.title }
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
                                expensesWithLocation.forEach { exp ->
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(exp.latitude!!, exp.longitude!!))
                                            .title(exp.title)
                                            .snippet("${currencyFormat.format(exp.amount)} € • ${exp.locationName ?: "Standort"}")
                                            .icon(createRedCircleIcon(exp.amount, context))
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
                }
            }
        }

        item {
            Text(
                text = "Orte & Kosten-Rangliste",
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
                            text = exp.locationName ?: "Unbekannter Ort",
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
                        Text(
                            text = "${currencyFormat.format(exp.amount)} €",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (exp.amount > 150.0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
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

private fun createRedCircleIcon(amount: Double, context: android.content.Context): org.maplibre.android.annotations.Icon {
    val amt = amount.coerceAtLeast(1.0)
    // Scale smoothly from 24px to 56px based on square root of amount
    val size = (20.0 + Math.sqrt(amt) * 2.0).coerceIn(24.0, 56.0).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(210, 239, 68, 68) // red with slight translucency
        style = android.graphics.Paint.Style.FILL
    }
    val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f
    }

    val radius = (size / 2f) - 2f
    canvas.drawCircle(size / 2f, size / 2f, radius, fillPaint)
    canvas.drawCircle(size / 2f, size / 2f, radius, strokePaint)

    return org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
}
