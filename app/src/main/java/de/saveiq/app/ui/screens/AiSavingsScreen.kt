package de.saveiq.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import de.saveiq.app.model.AppLanguage
import de.saveiq.app.model.AppStrings
import de.saveiq.app.model.PurchaseEmotions
import de.saveiq.app.ui.components.SaveIQAppIcon
import de.saveiq.app.ui.viewmodel.ExpenseViewModel
import de.saveiq.app.ui.viewmodel.TimeFilter
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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isEn = currentLanguage.isEnglish

    val currencyFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val wholeNumberFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
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
    val aiQuotes = remember(isEn) {
        if (isEn) {
            listOf(
                "“That coffee will cost you €24.50 in 30 years—money missing from your retirement nest egg.”",
                "“Online shopping carts at 11:00 PM are rarely a rational life decision.”",
                "“Every impulse buy avoided is a fully paid vacation day in your future.”",
                "“Frustration fades in two hours; your credit card bill stays all month.”",
                "“Your future self thanks you every time you simply close the tab.”",
                "“Spending money on things you don't need to impress people you don't like.”"
            )
        } else {
            listOf(
                "„Dieser Kaffee kostet dich in 30 Jahren umgerechnet 24,50 €, die dir in deiner Altersvorsorge fehlen.“",
                "„Online-Warenkörbe um 23:00 Uhr sind selten eine rationale Lebensentscheidung.“",
                "„Jeder nicht getätigte Frustkauf ist ein voll bezahlter Urlaubstag in deiner Zukunft.“",
                "„Frust vergeht nach zwei Stunden – die Kreditkartenabrechnung bleibt den ganzen Monat.“",
                "„Dein Zukunfts-Ich dankt dir jedes Mal, wenn du den Warenkorb einfach schließt.“",
                "„Geld ausgeben für Dinge, die man nicht braucht, um Menschen zu imponieren, die man nicht mag.“"
            )
        }
    }
    var currentQuoteIndex by remember { mutableIntStateOf(0) }

    // Bei jedem Öffnen des Tabs Denkanstöße neu würfeln & Inflation aktualisieren
    LaunchedEffect(Unit) {
        currentQuoteIndex = aiQuotes.indices.random()
        viewModel.refreshInflationRate(forceRefresh = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER (Ohne AI-Core Zeichen)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AppStrings.sparKiTitle(currentLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = AppStrings.sparKiSubtitle(currentLanguage),
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
                            label = { Text(filter.getLabel(currentLanguage)) },
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                            SaveIQAppIcon(size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Savings Status" else "Spar-Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = if (consciousPercentage >= 85) (if (isEn) "Master Saver" else "Meister-Sparfuchs")
                                    else (if (consciousPercentage >= 70) (if (isEn) "Mindful Spender" else "Bewusster Genießer") else (if (isEn) "Impulse Explorer" else "Impulsiver Entdecker")),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "$consciousPercentage % ${if (isEn) "Mindful" else "Bewusst"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (consciousPercentage >= 80) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Frust-Ausgaben Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "Impulse Cost" else "Frust-Kosten",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currencyFormat.format(totalFrustrationAmount)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalFrustrationAmount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${frustrationExpenses.size} ${if (isEn) "impulse buys" else "Fehlkäufe"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Hochrechnung 30-Jahre Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "30-Year Effect" else "30-Jahre-Effekt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val compound30Years = totalFrustrationAmount * ((1.0 + 0.07).pow(30))
                                Text(
                                    text = "≈ ${wholeNumberFormat.format(compound30Years)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = if (isEn) "At 7% World Bank ETF" else "Bei 7 % Weltbank-ETF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // LEBENSZEIT- & ARBEITSSTUNDEN-RECHNER (Modern, sauber gerahmt & dezente Schriftgrößen)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WorkHistory,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Life Energy & Work Hours" else "Lebenszeit- & Arbeitsrechner",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) "How many work hours did impulse buys cost? Select net wage:"
                                else "Arbeitszeit deiner Frustkäufe • Wähle Nettostundenlohn:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sauber gerahmte Segment-Auswahl für den Stundenlohn
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(15.0, 20.0, 25.0, 35.0).forEach { wage ->
                                val isSelected = hourlyWage == wage
                                val bgColor by animateColorAsState(
                                    if (isSelected) Color(0xFFD97706) else Color.Transparent,
                                    label = "wageBg"
                                )
                                val textColor by animateColorAsState(
                                    if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    label = "wageText"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bgColor)
                                        .clickable { hourlyWage = wage }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${wage.toInt()} €/h",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val workHoursLost = if (hourlyWage > 0) totalFrustrationAmount / hourlyWage else 0.0
                    val workDaysLost = workHoursLost / 8.0

                    // Zwei symmetrische, sauber gerahmte Kacheln (passend zum Spar-Status oben)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "Lost Work Time" else "Verlorene Arbeitszeit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(
                                        if (isEn) Locale.US else Locale.GERMANY,
                                        if (isEn) "%.1f Hours" else "%.1f Stunden",
                                        workHoursLost
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (workHoursLost > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) "From impulse buys" else "Durch Frustkäufe",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "Work Days (8h)" else "Arbeitstage (8h)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(
                                        if (isEn) Locale.US else Locale.GERMANY,
                                        if (isEn) "≈ %.1f Days" else "≈ %.1f Tage",
                                        workDaysLost
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                                Text(
                                    text = if (isEn) "At ${hourlyWage.toInt()} €/h net" else "Bei ${hourlyWage.toInt()} €/h Netto",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // INTERAKTIVER WAS-WÄRE-WENN CALCULATOR (Modern, sauber gerahmt & dezente Schriftgrößen)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.compoundInterestTitle(currentLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${AppStrings.worldBankInflation(worldBankInflationRate, currentLanguage)} • ${if (isEn) "Monthly savings:" else "Monatlicher Sparbetrag:"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sauber gerahmte Segment-Auswahl für den monatlichen Sparbetrag
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(25, 50, 100, 200).forEach { amount ->
                                val isSelected = selectedMonthlySavings == amount
                                val bgColor by animateColorAsState(
                                    if (isSelected) Color(0xFF0284C7) else Color.Transparent,
                                    label = "chipBg"
                                )
                                val textColor by animateColorAsState(
                                    if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    label = "chipText"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bgColor)
                                        .clickable { selectedMonthlySavings = amount }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$amount €",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val r = 0.07 / 12.0
                    val months10 = 120
                    val months25 = 300
                    val future10 = selectedMonthlySavings * (((1.0 + r).pow(months10) - 1.0) / r)
                    val future25 = selectedMonthlySavings * (((1.0 + r).pow(months25) - 1.0) / r)

                    val milestoneReward = if (isEn) {
                        when (selectedMonthlySavings) {
                            25 -> "✈️ Annual luxury city trip fully paid"
                            50 -> "🏖️ 1 full year sabbatical & travel by the ocean"
                            100 -> "⚡ Brand new dream car cash & debt-free"
                            else -> "🏡 Complete down payment for real estate"
                        }
                    } else {
                        when (selectedMonthlySavings) {
                            25 -> "✈️ Jährlicher Luxus-Städtetrip voll bezahlt"
                            50 -> "🏖️ 1 ganzes Jahr Auszeit & Reisen am Meer"
                            100 -> "⚡ Neuer Traumwagen bar & schuldenfrei"
                            else -> "🏡 Das komplette Eigenkapital für eine Immobilie"
                        }
                    }

                    // Zwei symmetrische, sauber gerahmte Kacheln (passend zum Rest der App)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "In 10 years" else "In 10 Jahren",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "≈ ${wholeNumberFormat.format(future10)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = if (isEn) "At 7% return p.a." else "Bei 7 % Rendite p.a.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isEn) "In 25 years" else "In 25 Jahren",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "≈ ${wholeNumberFormat.format(future25)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = if (isEn) "Compound growth" else "Mit Zinseszins-Effekt",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = milestoneReward,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
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
                                text = if (isEn) "AI Spark of Thought" else "KI-Denkanstoß",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                                contentDescription = if (isEn) "Next quote" else "Anderer Spruch",
                                tint = MaterialTheme.colorScheme.outline,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        text = AppStrings.savingsHacks(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) "Tap for details" else "Tippen für Info",
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
                        title = if (isEn) "48h Pause" else "48h-Pause",
                        subtitle = if (isEn) "Wait 2 days on impulse buys" else "Spontankäufe 2 Tage warten",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = if (isEn) {
                                SparHackDetail(
                                    emoji = "⏱️",
                                    title = "The 48-Hour Rule",
                                    subtitle = "Pause spontaneous buys for 2 days",
                                    explanation = "Behavioral economists prove: Over 70% of impulse buys lose their appeal within 48 hours.\n\nThe initial dopamine rush fades after two days, restoring clear rational decision making.",
                                    keyRule = "Rule: Any non-essential item over €20 goes onto a 48-hour waitlist. Only buy if the urge persists after two full days."
                                )
                            } else {
                                SparHackDetail(
                                    emoji = "⏱️",
                                    title = "Die 48-Stunden-Regel",
                                    subtitle = "Spontankäufe 2 Tage pausieren",
                                    explanation = "Verhaltensökonomen belegen: Über 70 % aller emotionalen Spontankäufe verlieren nach 48 Stunden ihren Reiz.\n\nBeim Gedanken an einen Kauf schüttet das Gehirn kurzfristig Dopamin aus. Nach zwei Tagen normalisiert sich der Spiegel wieder und du entscheidest mit klarem Kopf.",
                                    keyRule = "Regel: Alles ab 20 €, was nicht überlebenswichtig ist, 48 Stunden auf die Bedenkliste setzen. Erst kaufen, wenn der Wunsch nach 2 Tagen noch immer da ist."
                                )
                            }
                        }
                    )

                    SparHackGridButton(
                        emoji = "🚶",
                        title = if (isEn) "Frustration Walk" else "Frust-Lauf",
                        subtitle = if (isEn) "Steps instead of shopping" else "Schritte statt Shopping",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = if (isEn) {
                                SparHackDetail(
                                    emoji = "🚶",
                                    title = "The Frustration Walk",
                                    subtitle = "Collect steps instead of spending",
                                    explanation = "Stress or anger drives quick dopamine cravings—often leading to smartphone impulse purchases.\n\nA 15-minute brisk walk outdoors cuts cortisol stress hormone by up to 40% and releases endorphins—completely free.",
                                    keyRule = "Rule: When stressed, put the phone away, lace up your shoes, and take a 15-minute brisk walk before any purchase."
                                )
                            } else {
                                SparHackDetail(
                                    emoji = "🚶",
                                    title = "Der Frust-Spaziergang",
                                    subtitle = "Schritte sammeln statt Geld ausgeben",
                                    explanation = "Frust, Streit oder Erschöpfung erzeugen einen Drang nach schneller Belohnung – oft durch 'Frustkäufe' am Smartphone.\n\nEin 15-minütiger Spaziergang an der frischen Luft baut das Stresshormon Cortisol nachweislich um bis zu 40 % ab und setzt natürliche Endorphine frei – völlig kostenlos.",
                                    keyRule = "Regel: Bei Frust sofort das Smartphone weglegen, Schuhe anziehen und 15 Minuten stramm spazieren gehen, bevor eine Kaufentscheidung fällt."
                                )
                            }
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
                        title = if (isEn) "€30 Limit" else "30€ Limit",
                        subtitle = if (isEn) "Cap impulse buys strictly in cash" else "Emotionale Käufe bar deckeln",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = if (isEn) {
                                SparHackDetail(
                                    emoji = "💵",
                                    title = "The €30 Cash Budget",
                                    subtitle = "Strictly limit emotional purchases to cash",
                                    explanation = "Card payments numb the pain of paying. Total deprivation leads to relapse.\n\nWithdraw exactly €30 in physical cash each month dedicated for spontaneous treats.",
                                    keyRule = "Rule: Once the €30 bill is spent, absolutely no more spontaneous treats until next month."
                                )
                            } else {
                                SparHackDetail(
                                    emoji = "💵",
                                    title = "Das 30€ Bar-Budget",
                                    subtitle = "Spontanausgaben strikt bar begrenzen",
                                    explanation = "Kartenzahlung entkoppelt das Gehirn vom realen Schmerz des Geldverlusts ('Pain of Paying'). Völliger Verzicht führt jedoch zu Rückfällen.\n\nReserviere dir jeden Monat exakt 30 € in bar für Spontanausgaben und Belohnungen – ohne Karte.",
                                    keyRule = "Regel: Ist der 30€-Bargeldschein aufgebraucht, gibt es bis zum nächsten Monat absolut keine spontanen Belohnungskäufe mehr."
                                )
                            }
                        }
                    )

                    SparHackGridButton(
                        emoji = "🧘",
                        title = "HALT-Check",
                        subtitle = "Hungry, Angry, Lonely, Tired",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedHackDetail = if (isEn) {
                                SparHackDetail(
                                    emoji = "🧘",
                                    title = "The HALT Check",
                                    subtitle = "Hungry, Angry, Lonely, Tired",
                                    explanation = "The HALT method uncovers the root emotional causes of spontaneous urges:\n\n• H (Hungry): Low blood sugar impairs willpower.\n• A (Angry): Anger and frustration seek an outlet through spending.\n• L (Lonely / Bored): Loneliness is compensated with packages.\n• T (Tired): After 8 PM, self-control is exhausted.",
                                    keyRule = "Rule: Ask yourself before clicking: Am I Hungry, Angry, Lonely, or Tired? Address the real need instead of spending money."
                                )
                            } else {
                                SparHackDetail(
                                    emoji = "🧘",
                                    title = "Der HALT-Check",
                                    subtitle = "Hungry, Angry, Lonely, Tired",
                                    explanation = "Die HALT-Methode enttarnt die wahren Ursachen spontanen Kaufdrangs:\n\n• H (Hungry): Niedriger Blutzucker schwächt die Willenskraft.\n• A (Angry): Frust und Ärger suchen ein Ventil im Konsum.\n• L (Lonely / Bored): Einsamkeit und Langeweile füllt man mit Paketen.\n• T (Tired): Nach 20 Uhr ist die Selbstbeherrschung erschöpft.",
                                    keyRule = "Regel: Frage dich vor dem Klick: Bin ich Hungrig, Wütend, Einsam oder Müde? Behebe das echte Bedürfnis statt Geld auszugeben."
                                )
                            }
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
                        text = if (isEn) "Recorded Impulse Purchases" else "Erfasste Fehl- & Frustkäufe",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AppStrings.entriesCount(frustrationExpenses.size, currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            items(frustrationExpenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                            color = Color(0xFFDC2626).copy(alpha = 0.12f),
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
                    }
                }
            }
        }

        // RECHTLICHER HAFTUNGSAUSSCHLUSS / DISCLAIMER (Ganz unten & ganz klein)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isEn) {
                    "Disclaimer: All calculations, compound interest models, and AI sparks of thought serve exclusively for personal information and entertainment. They do not constitute financial advice or investment recommendations. Historical returns and inflation rates from the World Bank are simulations and provide no guarantee for future performance."
                } else {
                    "Haftungsausschluss: Alle Berechnungen, Zinseszins-Simulationen und KI-Denkanstöße dienen ausschließlich privaten Informations- und Unterhaltungszwecken und stellen keine Anlageberatung oder Finanzdienstleistung dar. Historische Renditen und Inflationsdaten der Weltbank sind Modellrechnungen und bieten keine Garantie für die Zukunft."
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
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
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = detail.keyRule,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    Text(AppStrings.understood(currentLanguage))
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = 0.dp
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
