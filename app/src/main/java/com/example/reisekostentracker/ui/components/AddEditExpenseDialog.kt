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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reisekostentracker.model.AppLanguage
import com.example.reisekostentracker.model.AppStrings
import com.example.reisekostentracker.model.Expense
import com.example.reisekostentracker.model.ExpenseCategory
import com.example.reisekostentracker.model.PurchaseEmotions
import com.example.reisekostentracker.model.RecurringInterval
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
    language: AppLanguage = AppLanguage.DE,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (
        title: String,
        amount: Double,
        currency: String,
        categoryId: String,
        tripId: String?,
        date: Long,
        notes: String,
        paymentMethod: String,
        locationName: String?,
        feelingEmoji: String?,
        isRecurring: Boolean,
        recurringInterval: RecurringInterval
    ) -> Unit
) {
    val isEn = language.isEnglish
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var amountText by remember {
        mutableStateOf(
            if (initialExpense != null) String.format(if (isEn) Locale.US else Locale.GERMAN, "%.2f", initialExpense.amount)
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
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: if (isEn) "Card" else "Karte") }
    var feelingEmoji by remember { mutableStateOf(initialExpense?.feelingEmoji) }
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

    val locale = if (isEn) Locale.ENGLISH else Locale.GERMAN
    val dateFormat = remember(isEn) { SimpleDateFormat(if (isEn) "MMMM dd, yyyy" else "dd. MMMM yyyy", locale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialExpense == null) AppStrings.newExpense(language) else AppStrings.editExpense(language),
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
                    label = { Text(if (isEn) "Title (e.g. Groceries, Rent, Coffee)" else "Titel (z.B. Supermarkt, Miete, Kaffee)") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text(if (isEn) "Please enter a title" else "Bitte einen Titel eingeben") }
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
                    label = { Text(AppStrings.amountInEuro(language)) },
                    leadingIcon = { Icon(Icons.Default.Euro, contentDescription = null) },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(if (isEn) "Enter valid amount (e.g. 12.50)" else "Gültigen Betrag eingeben (z.B. 12,50)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                Text(
                    text = AppStrings.category(language),
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
                            label = { Text(AppStrings.categoryName(cat.id, cat.name, language)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 10 Emojis für Kaufempfindungen (Frustkauf, Zufriedenheit, Gönnung...)
                if (PurchaseEmotions.shouldAskEmotion(selectedCategoryId)) {
                    Text(
                        text = if (isEn) "How did you feel about this purchase?" else "Wie hast du diesen Kauf empfunden?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEn) "Select from 10 emojis (AI impulse & frustration analysis):" else "Wähle aus 10 Emojis (KI-Analyse für Frust- & Impulskäufe):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PurchaseEmotions.list) { emotion ->
                            val isSelected = feelingEmoji == emotion.emoji
                            val emotionTitle = emotion.localizedTitle(isEn)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.clickable {
                                    feelingEmoji = if (feelingEmoji == emotion.emoji) null else emotion.emoji
                                }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(text = emotion.emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = emotionTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    feelingEmoji?.let { emoji ->
                        val emotion = PurchaseEmotions.fromEmoji(emoji)
                        if (emotion != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${emotion.emoji} ${emotion.localizedTitle(isEn)}: ${emotion.localizedDescription(isEn)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (emotion.isFrustrationOrImpulse) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🏠", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Housing & groceries: Emotional rating exempt." else "Miete & Grundnahrungsmittel: Emotionale Kaufbewertung entfällt.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Chips
                Text(
                    text = AppStrings.paymentMethod(language),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val methods = if (isEn) listOf("Card", "Cash", "Online") else listOf("Karte", "Bar", "Online")
                    methods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod.equals(method, ignoreCase = true) ||
                                    (isEn && method == "Card" && paymentMethod.equals("Karte", true)) ||
                                    (!isEn && method == "Karte" && paymentMethod.equals("Card", true)),
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date
                Text(
                    text = "${AppStrings.date(language)}: ${dateFormat.format(Date(selectedDate))}",
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
                        label = { Text(if (isEn) "Today" else "Heute") }
                    )
                    FilterChip(
                        selected = dateFormat.format(Date(selectedDate)) == dateFormat.format(Date(yesterdayMillis)),
                        onClick = { selectedDate = yesterdayMillis },
                        label = { Text(if (isEn) "Yesterday" else "Gestern") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location / Ort Field (Standard: GPS-Ort)
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(AppStrings.locationOptional(language)) },
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
                                    contentDescription = if (isEn) "Use current GPS location" else "GPS-Standort übernehmen",
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
                    val cities = listOf("Karlsruhe", "München", "Berlin", "Hamburg", "Köln", "Frankfurt", "Stuttgart", "Wien", "Zürich", "London", "Paris", "New York")
                    items(cities) { city ->
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
                    label = { Text(AppStrings.notesOptional(language)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recurring Expense Section (Dauerauftrag / Automatische Erfassung)
                if (initialExpense == null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.recurringExpenses(language),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AppStrings.recurringIntervalSub(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Switch(
                                    checked = isRecurring,
                                    onCheckedChange = { isRecurring = it }
                                )
                            }

                            if (isRecurring) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${AppStrings.interval(language)}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RecurringInterval.values().forEach { interval ->
                                        FilterChip(
                                            selected = recurringInterval == interval,
                                            onClick = { recurringInterval = interval },
                                            label = {
                                                Text(
                                                    text = interval.label(isEn),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (initialExpense != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = AppStrings.delete(language),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.delete(language))
                    }
                }

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
                                null,
                                selectedDate,
                                notes.trim(),
                                paymentMethod,
                                locationName.trim().ifEmpty { null },
                                feelingEmoji,
                                isRecurring,
                                recurringInterval
                            )
                        }
                    }
                ) {
                    Text(if (initialExpense == null) AppStrings.add(language) else AppStrings.save(language))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancel(language))
            }
        }
    )
}
