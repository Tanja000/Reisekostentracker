package de.saveiq.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.saveiq.app.model.AppLanguage
import de.saveiq.app.model.AppStrings
import de.saveiq.app.model.Expense
import de.saveiq.app.model.ExpenseCategory
import de.saveiq.app.model.RecurringExpense
import de.saveiq.app.model.RecurringInterval
import de.saveiq.app.ui.components.CategoryChip
import de.saveiq.app.ui.components.CategoryIconBadge
import de.saveiq.app.ui.components.QuickAddConfirmDialog
import de.saveiq.app.ui.components.QuickAddPreset
import de.saveiq.app.ui.components.QuickAddPresets
import de.saveiq.app.ui.viewmodel.ExpenseViewModel
import de.saveiq.app.ui.viewmodel.TimeFilter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.filteredExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val lastLocationName by viewModel.lastLocationName.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val recurringExpenses by viewModel.recurringExpenses.collectAsState()
    var activeQuickAdd by remember { mutableStateOf<QuickAddPreset?>(null) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val currencyFormat = remember(currentLanguage) {
        val locale = if (currentLanguage.isEnglish) Locale.US else Locale.GERMANY
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val dateFormat = remember(currentLanguage) {
        val locale = if (currentLanguage.isEnglish) Locale.US else Locale.GERMAN
        SimpleDateFormat(if (currentLanguage.isEnglish) "MMM dd, yyyy" else "dd. MMM yyyy", locale)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddExpenseDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ausgabe hinzufügen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Section: Total Spent Summary Card & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val totalSum = expenses.sumOf { it.amount }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${AppStrings.totalExpenses(currentLanguage)} (${timeFilter.getLabel(currentLanguage)})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${currencyFormat.format(totalSum)} €",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = AppStrings.entriesCount(expenses.size, currentLanguage),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(AppStrings.searchPlaceholder(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = AppStrings.delete(currentLanguage))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick-Add Horizontal Row with Recurring Button
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.quickAdd(currentLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Recurring Expenses Management Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.clickable { showRecurringDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${AppStrings.recurring(currentLanguage)} (${recurringExpenses.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QuickAddPresets.items) { preset ->
                        Surface(
                            modifier = Modifier.clickable { activeQuickAdd = preset },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.localizedTitle(currentLanguage.isEnglish),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currencyFormat.format(preset.defaultAmount)} €",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Time Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimeFilter.values()) { filter ->
                    FilterChip(
                        selected = timeFilter == filter,
                        onClick = { viewModel.setTimeFilter(filter) },
                        label = { Text(filter.getLabel(currentLanguage)) }
                    )
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text(if (currentLanguage.isEnglish) "All Categories" else "Alle Kategorien") }
                    )
                }
                items(categories) { cat ->
                    val displayName = AppStrings.categoryName(cat.id, cat.name, currentLanguage)
                    CategoryChip(
                        category = cat.copy(name = displayName),
                        isSelected = selectedCatId == cat.id,
                        onClick = {
                            viewModel.setCategoryFilter(if (selectedCatId == cat.id) null else cat.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // List of Expenses
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = AppStrings.noExpensesFound(currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.noExpensesHint(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.seedDemoData() }) {
                            Text(AppStrings.loadSampleData(currentLanguage))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        val cat = categoryMap[expense.categoryId] ?: ExpenseCategory(
                            id = expense.categoryId,
                            name = "Sonstiges",
                            iconName = "category",
                            colorValue = 0xFF757575L
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditExpenseDialog(expense) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIconBadge(category = cat, size = 44)

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    // Row 1: Titel links, Betrag rechts
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = expense.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!expense.feelingEmoji.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = expense.feelingEmoji,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${currencyFormat.format(expense.amount)} €",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Row 2: Kategorie • Datum • Ort • Zahlart • Löschen (alles horizontal lesbar)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = AppStrings.categoryName(cat.id, cat.name, currentLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cat.color,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " • ${dateFormat.format(Date(expense.date))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (!expense.locationName.isNullOrBlank()) {
                                            Text(
                                                text = " • 📍 ${expense.locationName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Auto-recorded indicator icon if notes indicate recurring/standing order
                                        if (expense.notes.contains("Dauerauftrag") || expense.notes.contains("Automatisch") || expense.notes.contains("Auto")) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(end = 4.dp)
                                            ) {
                                                Text(
                                                    text = "🔄",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = expense.paymentMethod,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (expense.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = expense.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick-Add Confirmation & Customization Dialog
    activeQuickAdd?.let { preset ->
        val bookedTitle = preset.localizedTitle(currentLanguage.isEnglish)
        QuickAddConfirmDialog(
            preset = preset,
            currentLocationName = lastLocationName,
            language = currentLanguage,
            onDismiss = { activeQuickAdd = null },
            onConfirm = { amount, method, location, feelingEmoji ->
                viewModel.quickAddExpense(
                    title = bookedTitle,
                    amount = amount,
                    categoryId = preset.categoryId,
                    paymentMethod = method,
                    customLocation = location,
                    feelingEmoji = feelingEmoji
                )
                Toast.makeText(
                    context,
                    if (currentLanguage.isEnglish) "✓ Booked: $bookedTitle (${currencyFormat.format(amount)} € in $location)"
                    else "✓ Gebucht: $bookedTitle (${currencyFormat.format(amount)} € in $location)",
                    Toast.LENGTH_SHORT
                ).show()
                activeQuickAdd = null
            }
        )
    }

    // Recurring Expenses Management Dialog
    if (showRecurringDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.recurringExpenses(currentLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = AppStrings.recurringSubtitle(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (recurringExpenses.isEmpty()) {
                        Text(
                            text = AppStrings.noRecurringExpenses(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        recurringExpenses.forEach { rec ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rec.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${currencyFormat.format(rec.amount)} €",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = rec.interval.label(currentLanguage.isEnglish),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${AppStrings.nextBooking(currentLanguage)}: ${dateFormat.format(Date(rec.nextDueDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                viewModel.triggerRecurringExpenseNow(rec.id)
                                                Toast.makeText(
                                                    context,
                                                    if (currentLanguage.isEnglish) "✓ Booked: ${rec.title}" else "✓ Gebucht: ${rec.title}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(AppStrings.bookNow(currentLanguage))
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteRecurringExpense(rec.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = AppStrings.delete(currentLanguage),
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
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
                Button(onClick = {
                    showRecurringDialog = false
                    showAddRecurringDialog = true
                }) {
                    Text(AppStrings.addRecurringTitle(currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurringDialog = false }) {
                    Text(AppStrings.understood(currentLanguage))
                }
            }
        )
    }

    // Add Recurring Expense Dialog
    if (showAddRecurringDialog) {
        var recTitle by remember { mutableStateOf("") }
        var recAmountText by remember { mutableStateOf("") }
        var recCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_sonstiges") }
        var recInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
        var recNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = {
                Text(
                    text = AppStrings.addRecurringTitle(currentLanguage),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = recTitle,
                        onValueChange = { recTitle = it },
                        label = { Text(if (currentLanguage.isEnglish) "Title (e.g. Rent, Gym, Spotify)" else "Titel (z.B. Miete, Gym, Spotify)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = recAmountText,
                        onValueChange = { recAmountText = it },
                        label = { Text(if (currentLanguage.isEnglish) "Amount in €" else "Betrag in €") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = AppStrings.category(currentLanguage),
                        style = MaterialTheme.typography.labelMedium,
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
                                selected = recCategory == cat.id,
                                onClick = { recCategory = cat.id },
                                label = { Text(AppStrings.categoryName(cat.id, cat.name, currentLanguage)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (currentLanguage.isEnglish) "Interval:" else "Intervall:",
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
                                selected = recInterval == interval,
                                onClick = { recInterval = interval },
                                label = { Text(interval.label(currentLanguage.isEnglish)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = recNotes,
                        onValueChange = { recNotes = it },
                        label = { Text(if (currentLanguage.isEnglish) "Notes (optional)" else "Notizen (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = recAmountText.replace(",", ".").toDoubleOrNull()
                        if (recTitle.isNotBlank() && amount != null && amount > 0.0) {
                            viewModel.addRecurringExpense(
                                title = recTitle.trim(),
                                amount = amount,
                                categoryId = recCategory,
                                interval = recInterval,
                                notes = recNotes.trim()
                            )
                            showAddRecurringDialog = false
                            Toast.makeText(
                                context,
                                if (currentLanguage.isEnglish) "✓ Recurring expense saved!" else "✓ Dauerauftrag gespeichert!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(AppStrings.save(currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecurringDialog = false }) {
                    Text(AppStrings.cancel(currentLanguage))
                }
            }
        )
    }
}
