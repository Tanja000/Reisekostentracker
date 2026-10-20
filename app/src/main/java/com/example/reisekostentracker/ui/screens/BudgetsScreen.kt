package com.example.reisekostentracker.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reisekostentracker.ui.components.QuickAddConfirmDialog
import com.example.reisekostentracker.ui.components.QuickAddPreset
import com.example.reisekostentracker.ui.components.QuickAddPresets
import com.example.reisekostentracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BudgetsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val lastLocationName by viewModel.lastLocationName.collectAsState()
    val context = LocalContext.current

    var monthlyBudget by remember { mutableDoubleStateOf(1500.0) }
    var isEditBudgetDialogOpen by remember { mutableStateOf(false) }
    var activeQuickAdd by remember { mutableStateOf<QuickAddPreset?>(null) }

    val currencyFormat = remember {
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    // Calculate Current Month Expenses
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val thisMonthExpenses = expenses.filter { exp ->
        val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
        expCal.get(Calendar.MONTH) == currentMonth && expCal.get(Calendar.YEAR) == currentYear
    }

    val spentThisMonth = thisMonthExpenses.sumOf { it.amount }
    val remainingBudget = monthlyBudget - spentThisMonth
    val budgetProgress = if (monthlyBudget > 0) (spentThisMonth / monthlyBudget).toFloat().coerceIn(0f, 1.5f) else 0f
    val dailyAllowance = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0

    // Payment Methods breakdown
    val payMethodTotals = expenses.groupBy { it.paymentMethod }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Budgets & Finanzen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Monatsbudget, Schnellbuchung & Wallets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = { isEditBudgetDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Budget anpassen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Monthly Budget Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Monatsbudget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${currencyFormat.format(monthlyBudget)} €",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                val progressColor = when {
                    budgetProgress > 1.0f -> MaterialTheme.colorScheme.error
                    budgetProgress > 0.8f -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.primary
                }

                LinearProgressIndicator(
                    progress = { budgetProgress.coerceAtMost(1.0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ausgegeben (${(budgetProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${currencyFormat.format(spentThisMonth)} €",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (remainingBudget >= 0) "Restbudget" else "Überzogen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (remainingBudget >= 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${currencyFormat.format(remainingBudget)} €",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily allowance chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tagesbudget ($daysRemaining Tage verbleibend):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "~ ${currencyFormat.format(dailyAllowance)} € / Tag",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Quick-Add Section (1-Tap Bookings)
        Text(
            text = "Schnellbuchung (1-Klick)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Häufige Alltagsausgaben sofort eintragen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAddButton(
                icon = Icons.Default.LocalCafe,
                label = "Kaffee & Snack",
                amount = "3,80 €",
                modifier = Modifier.weight(1f),
                onClick = { activeQuickAdd = QuickAddPresets.items[0] }
            )

            QuickAddButton(
                icon = Icons.Default.Fastfood,
                label = "Mittagessen",
                amount = "12,50 €",
                modifier = Modifier.weight(1f),
                onClick = { activeQuickAdd = QuickAddPresets.items[1] }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAddButton(
                icon = Icons.Default.ShoppingCart,
                label = "Supermarkt",
                amount = "35,00 €",
                modifier = Modifier.weight(1f),
                onClick = { activeQuickAdd = QuickAddPresets.items[2] }
            )

            QuickAddButton(
                icon = Icons.Default.DirectionsCar,
                label = "Tanken / Ticket",
                amount = "25,00 €",
                modifier = Modifier.weight(1f),
                onClick = { activeQuickAdd = QuickAddPresets.items[3] }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Wallets & Payment Methods
        Text(
            text = "Zahlungsarten & Wallets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                listOf(
                    Triple("Karte", "Girokonto & Kreditkarten", Icons.Default.CreditCard),
                    Triple("Bar", "Bargeld & Münzen", Icons.Default.Payments),
                    Triple("Online", "PayPal, Online & Abos", Icons.Default.AttachMoney)
                ).forEachIndexed { idx, (key, sub, icon) ->
                    val sum = payMethodTotals[key] ?: 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = key, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Text(
                            text = "${currencyFormat.format(sum)} €",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (idx < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. CSV Export Button
        OutlinedButton(
            onClick = {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)
                val csvHeader = "ID;Titel;Betrag;Waehrung;Datum;Kategorie;Zahlart;Ort;Notiz\n"
                val csvBody = expenses.joinToString("\n") { e ->
                    "${e.id};${e.title};${e.amount};${e.currency};${dateFormat.format(Date(e.date))};${e.categoryId};${e.paymentMethod};${e.locationName ?: ""};${e.notes}"
                }
                val fullCsv = csvHeader + csvBody

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, fullCsv)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "TrackMyCent-Ausgabenexport.csv")
                }
                val shareIntent = Intent.createChooser(sendIntent, "TrackMyCent CSV exportieren")
                context.startActivity(shareIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alle Finanzen als CSV exportieren")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Edit Budget Dialog
    if (isEditBudgetDialogOpen) {
        var budgetText by remember { mutableStateOf(monthlyBudget.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { isEditBudgetDialogOpen = false },
            title = { Text("Monatsbudget festlegen", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Budget in Euro (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    monthlyBudget = budgetText.toDoubleOrNull() ?: monthlyBudget
                    isEditBudgetDialogOpen = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditBudgetDialogOpen = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Quick-Add Confirmation & Customization Dialog
    activeQuickAdd?.let { preset ->
        QuickAddConfirmDialog(
            preset = preset,
            currentLocationName = lastLocationName,
            onDismiss = { activeQuickAdd = null },
            onConfirm = { amount, method ->
                viewModel.quickAddExpense(
                    title = preset.title,
                    amount = amount,
                    categoryId = preset.categoryId,
                    paymentMethod = method
                )
                Toast.makeText(
                    context,
                    "✓ Gebucht: ${preset.title} (${currencyFormat.format(amount)} €)",
                    Toast.LENGTH_SHORT
                ).show()
                activeQuickAdd = null
            }
        )
    }
}

@Composable
private fun QuickAddButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
