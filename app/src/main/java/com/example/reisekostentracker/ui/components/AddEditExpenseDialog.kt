package com.example.reisekostentracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.reisekostentracker.model.Expense
import com.example.reisekostentracker.model.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseDialog(
    initialExpense: Expense?,
    categories: List<ExpenseCategory>,
    currentLocationName: String? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        currency: String,
        categoryId: String,
        tripId: String?,
        date: Long,
        notes: String,
        paymentMethod: String,
        locationName: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (initialExpense != null) String.format(Locale.GERMAN, "%.2f", initialExpense.amount)
            else ""
        )
    }
    var selectedCategoryId by remember {
        mutableStateOf(
            initialExpense?.categoryId ?: categories.firstOrNull()?.id ?: "cat_sonstiges"
        )
    }
    var selectedDate by remember { mutableStateOf(initialExpense?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: "Karte") }
    var locationName by remember {
        mutableStateOf(
            initialExpense?.locationName
                ?: if (!currentLocationName.isNullOrBlank() && !currentLocationName.contains("fehlt") && !currentLocationName.contains("ermittelt")) {
                    currentLocationName
                } else {
                    "Karlsruhe"
                }
        )
    }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialExpense == null) "Neue Ausgabe erfassen" else "Ausgabe bearbeiten",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Titel (z.B. Supermarkt, Miete, Kaffee)") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Bitte einen Titel eingeben") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        if (it.isNotBlank()) amountError = false
                    },
                    label = { Text("Betrag in €") },
                    leadingIcon = { Icon(Icons.Default.Euro, contentDescription = null) },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Gültigen Betrag eingeben (z.B. 12,50)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                Text(
                    text = "Kategorie",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = cat.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Chips
                Text(
                    text = "Zahlungsmethode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Karte", "Bar", "Online").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date
                Text(
                    text = "Datum: ${dateFormat.format(Date(selectedDate))}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val todayMillis = System.currentTimeMillis()
                    val yesterdayMillis = Calendar.getInstance().apply {
                        timeInMillis = todayMillis
                        add(Calendar.DAY_OF_YEAR, -1)
                    }.timeInMillis

                    FilterChip(
                        selected = dateFormat.format(Date(selectedDate)) == dateFormat.format(Date(todayMillis)),
                        onClick = { selectedDate = todayMillis },
                        label = { Text("Heute") }
                    )
                    FilterChip(
                        selected = dateFormat.format(Date(selectedDate)) == dateFormat.format(Date(yesterdayMillis)),
                        onClick = { selectedDate = yesterdayMillis },
                        label = { Text("Gestern") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location / Ort Field (Standard: GPS-Ort)
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Ort / GPS-Standort") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (!currentLocationName.isNullOrBlank() && !currentLocationName.contains("fehlt") && !currentLocationName.contains("ermittelt")) {
                            IconButton(onClick = { locationName = currentLocationName }) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "GPS-Standort übernehmen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick city suggestions
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf("Karlsruhe", "München", "Berlin", "Hamburg", "Köln", "Frankfurt", "Stuttgart", "Wien", "Zürich")) { city ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (locationName == city) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { locationName = city }
                        ) {
                            Text(
                                text = city,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (locationName == city) FontWeight.Bold else FontWeight.Normal,
                                color = if (locationName == city) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notiz (optional)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull()
                    if (title.isBlank()) {
                        titleError = true
                    }
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = true
                    }
                    if (title.isNotBlank() && parsedAmount != null && parsedAmount > 0.0) {
                        onSave(
                            title.trim(),
                            parsedAmount,
                            "EUR",
                            selectedCategoryId,
                            null, // No trip association in TrackMyCent
                            selectedDate,
                            notes.trim(),
                            paymentMethod,
                            locationName.trim().ifEmpty { null }
                        )
                    }
                }
            ) {
                Text(if (initialExpense == null) "Hinzufügen" else "Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
