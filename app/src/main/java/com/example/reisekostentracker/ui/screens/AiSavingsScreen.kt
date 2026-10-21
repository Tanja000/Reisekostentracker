package com.example.reisekostentracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reisekostentracker.model.PurchaseEmotions
import com.example.reisekostentracker.ui.viewmodel.ExpenseViewModel
import com.example.reisekostentracker.ui.viewmodel.TimeFilter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

data class WishItem(
    val id: String,
    val title: String,
    val price: Double,
    val hoursLeft: Int
)

@Composable
fun AiSavingsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val worldBankInflationRate by viewModel.worldBankInflationRate.collectAsState()

    val currencyFormat = remember {
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val wholeNumberFormat = remember {
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            maximumFractionDigits = 0
        }
    }

    // Filter frustration purchases
    val frustrationExpenses = remember(expenses) {
        expenses.filter { exp ->
            exp.feelingEmoji != null && PurchaseEmotions.isFrustrationPurchase(exp.feelingEmoji)
        }
    }

    val totalFrustrationAmount = remember(frustrationExpenses) {
        frustrationExpenses.sumOf { it.amount }
    }

    val totalExpensesAmount = remember(expenses) {
        expenses.sumOf { it.amount }
    }

    val consciousPercentage = remember(totalExpensesAmount, totalFrustrationAmount) {
        if (totalExpensesAmount > 0) {
            ((1.0 - (totalFrustrationAmount / totalExpensesAmount)) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }
    }

    // Interactive Savings Simulator State (e.g. 50€/Month)
    var selectedMonthlySavings by remember { mutableIntStateOf(50) }

    // Hourly Wage State for Life Energy Cost
    var hourlyWage by remember { mutableDoubleStateOf(20.0) }

    // Dialog States for the 3 Interactive Hacks
    var showHack1Dialog by remember { mutableStateOf(false) }
    var showHack2Dialog by remember { mutableStateOf(false) }
    var showHack3Dialog by remember { mutableStateOf(false) }

    // 48h Wishlist State
    val wishList = remember {
        mutableStateListOf(
            WishItem("1", "Kabellose Kopfhörer ANC", 89.0, 31),
            WishItem("2", "Trend-Sneaker Limited", 119.0, 14)
        )
    }
    var newWishTitle by remember { mutableStateOf("") }
    var newWishPrice by remember { mutableStateOf("") }

    // 30€ Cash Envelope State
    var cashEnvelopeSpent by remember { mutableDoubleStateOf(11.50) }

    // Witty Quotes Carousel State
    val aiQuotes = remember {
        listOf(
            "„Dieser Kaffee kostet dich in 30 Jahren umgerechnet 24,50 €, die dir in deiner Altersvorsorge fehlen.“",
            "„Online-Warenkörbe um 23:00 Uhr sind selten eine rationale Lebensentscheidung.“",
            "„Jeder nicht getätigte Frustkauf ist ein voll bezahlter Urlaubstag in deiner Zukunft.“",
            "„Frust vergeht nach zwei Stunden – die Kreditkartenabrechnung bleibt den ganzen Monat.“",
            "„Dein Zukunfts-Ich dankt dir jedes Mal, wenn du den Warenkorb einfach schließt.“",
            "„Geld ausgeben für Dinge, die man nicht braucht, um Menschen zu imponieren, die man nicht mag.“"
        )
    }
    var currentQuoteIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spar-KI & Sparfuchs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Intelligente Frustkauf-Vermeidung & Vermögens-Effekt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AICore",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeFilter.values().forEach { filter ->
                        FilterChip(
                            selected = timeFilter == filter,
                            onClick = { viewModel.setTimeFilter(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // GAMIFIED SPAR-SCORE & STATUS BANNER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🦊", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Spar-Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = if (consciousPercentage >= 85) "Meister-Sparfuchs" else (if (consciousPercentage >= 70) "Bewusster Genießer" else "Impulsiver Entdecker"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "$consciousPercentage % Bewusst",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (consciousPercentage >= 80) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Frust-Ausgaben Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Frust-Kosten",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currencyFormat.format(totalFrustrationAmount)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalFrustrationAmount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${frustrationExpenses.size} Fehlkäufe",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Hochrechnung 30-Jahre Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "30-Jahre-Effekt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val compound30Years = totalFrustrationAmount * ((1.0 + 0.07).pow(30))
                                Text(
                                    text = "≈ ${wholeNumberFormat.format(compound30Years)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = "Bei 7 % Weltbank-ETF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // RAFFINIERTES FEATURE 1: LEBENSZEIT- & ARBEITSSTUNDEN-RECHNER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkHistory,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lebenszeit- & Arbeitsrechner",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "Life Energy",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wie viele Stunden Lebenszeit kosten deine Frustkäufe? Wähle deinen Nettostundenlohn:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Wage Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15.0, 20.0, 25.0, 35.0).forEach { wage ->
                            val isSelected = hourlyWage == wage
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surface)
                                    .clickable { hourlyWage = wage }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${wage.toInt()} €/h",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val workHoursLost = if (hourlyWage > 0) totalFrustrationAmount / hourlyWage else 0.0
                    val workDaysLost = workHoursLost / 8.0

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Verlorene Arbeitszeit:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = String.format(Locale.GERMANY, "%.1f Stunden", workHoursLost),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFDC2626)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = String.format(Locale.GERMANY, "≈ %.1f Arbeitstage", workDaysLost),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // RAFFINIERTES FEATURE 2: INTERAKTIVER WAS-WÄRE-WENN ZINSESZINS & TRAUM-SIMULATOR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Was-Wäre-Wenn Zinseszins",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = "Weltbank ${String.format(Locale.US, "%.2f", worldBankInflationRate)}% Inf.",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wähle deinen monatlichen Sparbetrag aus Frustkauf-Vermeidung:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Savings Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(25, 50, 100, 200).forEach { amount ->
                            val isSelected = selectedMonthlySavings == amount
                            val bgColor by animateColorAsState(
                                if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surface,
                                label = "chipBg"
                            )
                            val textColor by animateColorAsState(
                                if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                label = "chipText"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .clickable { selectedMonthlySavings = amount }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$amount €",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Results Box with Fun Milestone
                    val r = 0.07 / 12.0
                    val months10 = 120
                    val months25 = 300
                    val future10 = selectedMonthlySavings * (((1.0 + r).pow(months10) - 1.0) / r)
                    val future25 = selectedMonthlySavings * (((1.0 + r).pow(months25) - 1.0) / r)

                    val milestoneReward = when (selectedMonthlySavings) {
                        25 -> "✈️ Jährlicher Luxus-Städtetrip voll bezahlt"
                        50 -> "🏖️ 1 ganzes Jahr Auszeit & Reisen am Meer"
                        100 -> "⚡ Neuer Traumwagen bar & schuldenfrei"
                        else -> "🏡 Das komplette Eigenkapital für eine Immobilie"
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "In 10 Jahren:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "≈ ${wholeNumberFormat.format(future10)} €",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "In 25 Jahren:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "≈ ${wholeNumberFormat.format(future25)} €",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = milestoneReward,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }
        }

        // UNTERHALTSAMER KI-SPRUCH DES TAGES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KI-Denkanstoß",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        IconButton(
                            onClick = {
                                currentQuoteIndex = (currentQuoteIndex + 1) % aiQuotes.size
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Casino,
                                contentDescription = "Anderer Spruch",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedContent(
                        targetState = aiQuotes[currentQuoteIndex],
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "quoteAnim"
                    ) { quote ->
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 3 INTERAKTIVE & ANKLICKBARE SPAR-HACKS (Jetzt als schöne, geordnete Klick-Karten!)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3 interaktive Spar-Hacks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tippen zum Öffnen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hack 1: 48h-Pause (Anklickbar)
                InteractiveHackRowCard(
                    emoji = "⏱️",
                    title = "48-Stunden Wunsch-Pause",
                    badge = "${wishList.size} aktiv",
                    desc = "Wunschkäufe 2 Tage pausieren. Hier tippen, um Spontankauf zu stoppen.",
                    onClick = { showHack1Dialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hack 2: Frust-Spaziergang (Anklickbar)
                InteractiveHackRowCard(
                    emoji = "🚶",
                    title = "Frust-Notbremse (HALT-Prinzip)",
                    badge = "Anti-Dopamin",
                    desc = "Hungrig, wütend, einsam oder müde? 15-Minuten-Gehirn-Reset starten.",
                    onClick = { showHack2Dialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hack 3: 30€ Belohnungs-Umschlag (Anklickbar)
                val remainingCash = (30.0 - cashEnvelopeSpent).coerceAtLeast(0.0)
                InteractiveHackRowCard(
                    emoji = "💵",
                    title = "30€ Bar-Umschlag",
                    badge = "${currencyFormat.format(remainingCash)} € übrig",
                    desc = "Monatsgrenze für Frust- & Belohnungskäufe. Tippen zum Verwalten.",
                    onClick = { showHack3Dialog = true }
                )
            }
        }

        // LIST OF FRUSTRATION PURCHASES
        if (frustrationExpenses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Erfasste Fehl- & Frustkäufe",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${frustrationExpenses.size} Einträge",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            items(frustrationExpenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = exp.feelingEmoji ?: "🤦",
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (exp.notes.isNotBlank() || !exp.locationName.isNullOrBlank()) {
                                Text(
                                    text = listOfNotNull(exp.notes.ifBlank { null }, exp.locationName?.let { "📍 $it" }).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${currencyFormat.format(exp.amount)} €",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { viewModel.deleteExpense(exp.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Löschen",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOG 1: 48h WUNSCHKAUF-PAUSE
    if (showHack1Dialog) {
        AlertDialog(
            onDismissRequest = { showHack1Dialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("48h-Wunschkauf-Pause")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Studien zeigen: 70 % aller Spontankäufe verlieren nach 48 Stunden ihren Reiz. Trage hier einen Wunsch ein, den du gerade kaufen wolltest:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newWishTitle,
                        onValueChange = { newWishTitle = it },
                        label = { Text("Was möchtest du kaufen?") },
                        placeholder = { Text("z.B. Neue Kopfhörer") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newWishPrice,
                            onValueChange = { newWishPrice = it },
                            label = { Text("Preis in €") },
                            placeholder = { Text("49.99") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val pr = newWishPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                                if (newWishTitle.isNotBlank() && pr > 0) {
                                    wishList.add(0, WishItem(System.currentTimeMillis().toString(), newWishTitle.trim(), pr, 48))
                                    newWishTitle = ""
                                    newWishPrice = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pausieren")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Laufende Bedenkzeiten (${wishList.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    wishList.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${currencyFormat.format(item.price)} € • Noch ${item.hoursLeft}h Bedenkzeit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { wishList.remove(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Gespart!", tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHack1Dialog = false }) {
                    Text("Fertig")
                }
            }
        )
    }

    // DIALOG 2: FRUST-NOTBREMSE & HALT-CHECK
    if (showHack2Dialog) {
        AlertDialog(
            onDismissRequest = { showHack2Dialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚶", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Frust-Notbremse (HALT)")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bevor du Geld ausgibst, prüfe das psychologische HALT-Prinzip. Warum willst du gerade kaufen?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HaltQuestionItem("🍔", "Hungry (Hungrig)?", "Ein kleiner Snack stoppt oft den Kaufreiz.")
                    Spacer(modifier = Modifier.height(6.dp))
                    HaltQuestionItem("😡", "Angry (Wütend)?", "Frust über die Arbeit? 15 min Spaziergang löst mehr.")
                    Spacer(modifier = Modifier.height(6.dp))
                    HaltQuestionItem("🥱", "Lonely / Bored (Langeweile)?", "Shopping als Zeitvertreib. Ruf stattdessen jemanden an.")
                    Spacer(modifier = Modifier.height(6.dp))
                    HaltQuestionItem("😴", "Tired (Müde)?", "Nach 21 Uhr ist deine Willenskraft erschöpft. Erst schlafen!")

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF15803D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tipp: Geh 15 Minuten vor die Tür. 95 % des Kaufdrangs verfliegen an der frischen Luft.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHack2Dialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Frust abgewendet!")
                }
            }
        )
    }

    // DIALOG 3: 30€ BAR-BELOHNUNGS-UMSCHLAG
    if (showHack3Dialog) {
        val budget = 30.0
        val remaining = (budget - cashEnvelopeSpent).coerceAtLeast(0.0)
        val progress = (cashEnvelopeSpent / budget).toFloat().coerceIn(0f, 1f)

        AlertDialog(
            onDismissRequest = { showHack3Dialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💵", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("30€ Bar-Belohnungs-Umschlag")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Völliger Verzicht führt zu Rückfällen. Erlaube dir jeden Monat genau 30 € in bar für emotionale Spontankäufe – strikt ohne Karte!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monats-Budget:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${currencyFormat.format(remaining)} € übrig", fontWeight = FontWeight.Bold, color = if (remaining > 5) Color(0xFF16A34A) else Color(0xFFDC2626))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress < 0.8f) Color(0xFF16A34A) else Color(0xFFDC2626),
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bisher verbraucht: ${currencyFormat.format(cashEnvelopeSpent)} € von 30,00 €",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Schnell-Abzug für Bar-Belohnung:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cashEnvelopeSpent = (cashEnvelopeSpent + 3.50).coerceAtMost(30.0) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("☕ Kaffee (3,50 €)", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { cashEnvelopeSpent = (cashEnvelopeSpent + 5.00).coerceAtMost(30.0) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🍫 Snack (5,00 €)", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHack3Dialog = false }) {
                    Text("Fertig")
                }
            },
            dismissButton = {
                TextButton(onClick = { cashEnvelopeSpent = 0.0 }) {
                    Text("Budget zurücksetzen")
                }
            }
        )
    }
}

@Composable
private fun InteractiveHackRowCard(
    emoji: String,
    title: String,
    badge: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 16.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Öffnen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HaltQuestionItem(emoji: String, question: String, tip: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(tip, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
