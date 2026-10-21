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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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

data class SparHackDetail(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val explanation: String,
    val keyRule: String
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

    // Selected Hack for Detail Dialog (Whole Info Only, No Sub-functions)
    var selectedHackDetail by remember { mutableStateOf<SparHackDetail?>(null) }

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

    // Bei jedem Öffnen des Tabs Denkanstöße neu würfeln & Inflation aktualisieren
    LaunchedEffect(Unit) {
        currentQuoteIndex = aiQuotes.indices.random()
        viewModel.refreshInflationRate(forceRefresh = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER (Ohne AI-Core Zeichen)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
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

        // LEBENSZEIT- & ARBEITSSTUNDEN-RECHNER
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Wie viele Arbeitsstunden kosten deine Frustkäufe? Wähle deinen Nettostundenlohn:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                                    .padding(vertical = 8.dp),
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

        // INTERAKTIVER WAS-WÄRE-WENN CALCULATOR (Saubere Überschrift & Inflation getrennt!)
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
                    // Headline mit separater Zeile für Inflation (Keine Überdeckung mehr!)
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = "Weltbank-Inflation DEU: ${String.format(Locale.US, "%.2f", worldBankInflationRate)} % (Live aktualisiert)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
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

        // UNTERHALTSAMER KI-SPRUCH DES TAGES (Neu gewürfelt bei Tab-Aufruf)
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

        // 2x2 MATRIX: SPAR-HACKS (Nur Info beim Anklicken, keine neue Funktion)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spar-Hacks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tippen für Info",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2 x 2 Matrix Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SparHackGridButton(
                        emoji = "⏱️",
                        title = "48h-Pause",
                        subtitle = "Spontankäufe 2 Tage warten",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = SparHackDetail(
                                emoji = "⏱️",
                                title = "Die 48-Stunden-Regel",
                                subtitle = "Spontankäufe 2 Tage pausieren",
                                explanation = "Verhaltensökonomen belegen: Über 70 % aller emotionalen Spontankäufe verlieren nach 48 Stunden ihren Reiz.\n\nBeim Gedanken an einen Kauf schüttet das Gehirn kurzfristig Dopamin aus. Nach zwei Tagen normalisiert sich der Spiegel wieder und du entscheidest mit klarem Kopf.",
                                keyRule = "Regel: Alles ab 20 €, was nicht überlebenswichtig ist, 48 Stunden auf die Bedenkliste setzen. Erst kaufen, wenn der Wunsch nach 2 Tagen noch immer da ist."
                            )
                        }
                    )

                    SparHackGridButton(
                        emoji = "🚶",
                        title = "Frust-Lauf",
                        subtitle = "Schritte statt Shopping",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = SparHackDetail(
                                emoji = "🚶",
                                title = "Der Frust-Spaziergang",
                                subtitle = "Schritte sammeln statt Geld ausgeben",
                                explanation = "Frust, Streit oder Erschöpfung erzeugen einen Drang nach schneller Belohnung – oft durch 'Frustkäufe' am Smartphone.\n\nEin 15-minütiger Spaziergang an der frischen Luft baut das Stresshormon Cortisol nachweislich um bis zu 40 % ab und setzt natürliche Endorphine frei – völlig kostenlos.",
                                keyRule = "Regel: Bei Frust sofort das Smartphone weglegen, Schuhe anziehen und 15 Minuten stramm spazieren gehen, bevor eine Kaufentscheidung fällt."
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2 x 2 Matrix Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SparHackGridButton(
                        emoji = "💵",
                        title = "30€ Limit",
                        subtitle = "Emotionale Käufe bar deckeln",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = SparHackDetail(
                                emoji = "💵",
                                title = "Das 30€ Bar-Budget",
                                subtitle = "Spontanausgaben strikt bar begrenzen",
                                explanation = "Kartenzahlung entkoppelt das Gehirn vom realen Schmerz des Geldverlusts ('Pain of Paying'). Völliger Verzicht führt jedoch zu Rückfällen.\n\nReserviere dir jeden Monat exakt 30 € in bar für Spontanausgaben und Belohnungen – ohne Karte.",
                                keyRule = "Regel: Ist der 30€-Bargeldschein aufgebraucht, gibt es bis zum nächsten Monat absolut keine spontanen Belohnungskäufe mehr."
                            )
                        }
                    )

                    SparHackGridButton(
                        emoji = "🧘",
                        title = "HALT-Check",
                        subtitle = "Hungry, Angry, Lonely, Tired",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = SparHackDetail(
                                emoji = "🧘",
                                title = "Der HALT-Check",
                                subtitle = "Hungry, Angry, Lonely, Tired",
                                explanation = "Die HALT-Methode enttarnt die wahren Ursachen spontanen Kaufdrangs:\n\n• H (Hungry): Niedriger Blutzucker schwächt die Willenskraft.\n• A (Angry): Frust und Ärger suchen ein Ventil im Konsum.\n• L (Lonely / Bored): Einsamkeit und Langeweile füllt man mit Paketen.\n• T (Tired): Nach 20 Uhr ist die Selbstbeherrschung erschöpft.",
                                keyRule = "Regel: Frage dich vor dem Klick: Bin ich Hungrig, Wütend, Einsam oder Müde? Behebe das echte Bedürfnis statt Geld auszugeben."
                            )
                        }
                    )
                }
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

    // DETAIL-DIALOG FÜR SPAR-HACKS (Nur die ganze Info, keine neuen Funktionen)
    selectedHackDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedHackDetail = null },
            icon = {
                Text(detail.emoji, fontSize = 28.sp)
            },
            title = {
                Text(
                    text = detail.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = detail.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = detail.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = detail.keyRule,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedHackDetail = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Verstanden")
                }
            }
        )
    }
}

@Composable
private fun SparHackGridButton(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
