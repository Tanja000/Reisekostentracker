package de.saveiq.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.saveiq.app.model.AppStrings
import de.saveiq.app.model.CategoryStat
import de.saveiq.app.model.DayStat
import de.saveiq.app.model.Expense
import de.saveiq.app.model.ExpenseStatistics
import de.saveiq.app.ui.components.CategoryIconBadge
import de.saveiq.app.ui.components.CategoryPieChart
import de.saveiq.app.ui.components.PaymentMethodsCard
import de.saveiq.app.ui.components.StatCard
import de.saveiq.app.ui.components.WeekdaySpendingChart
import de.saveiq.app.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

private enum class StatPeriodMode {
    ALL_TIME,       // Gesamt seit Aufzeichnung
    TODAY,          // Heutiger Tag
    THIS_WEEK,      // Heutige Woche
    YEAR_TO_DATE,   // Das Jahr bis jetzt
    SPECIFIC_YEAR,  // Einzelnes Jahr (wenn > 1 Jahr Daten vorhanden)
    SPECIFIC_MONTH  // Gewählter Monat aus dem Dropdown-Menü
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val allExpenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isEn = currentLanguage.isEnglish

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    // Ermittle alle Jahre, in denen Ausgaben existieren
    val availableYears = remember(allExpenses) {
        val years = allExpenses.map {
            Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.YEAR)
        }.distinct().sortedDescending()
        years.ifEmpty { listOf(currentYear) }
    }

    var periodMode by rememberSaveable { mutableStateOf(StatPeriodMode.ALL_TIME) }
    var selectedYear by rememberSaveable { mutableIntStateOf(currentYear) }
    // -1 = Kein einzelner Monat gewählt ("Alle Monate"), 0..11 = Januar..Dezember
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(-1) }
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }

    val monthNamesDe = remember {
        listOf(
            "Januar", "Februar", "März", "April", "Mai", "Juni",
            "Juli", "August", "September", "Oktober", "November", "Dezember"
        )
    }
    val monthNamesEn = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }
    val monthNames = if (isEn) monthNamesEn else monthNamesDe

    // Filtere die Ausgaben passend zum gewählten Zeitraum oder Monat
    val activeYearForMonth = if (availableYears.contains(selectedYear)) selectedYear else currentYear

    val filteredExpenses = remember(allExpenses, periodMode, selectedYear, selectedMonthIndex, activeYearForMonth) {
        val nowCal = Calendar.getInstance()
        val nowYear = nowCal.get(Calendar.YEAR)
        val nowDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR)
        val nowWeek = nowCal.get(Calendar.WEEK_OF_YEAR)

        allExpenses.filter { exp ->
            val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
            val expYear = expCal.get(Calendar.YEAR)
            val expMonth = expCal.get(Calendar.MONTH)

            when (periodMode) {
                StatPeriodMode.ALL_TIME -> true
                StatPeriodMode.TODAY -> {
                    expYear == nowYear && expCal.get(Calendar.DAY_OF_YEAR) == nowDayOfYear
                }
                StatPeriodMode.THIS_WEEK -> {
                    expYear == nowYear && expCal.get(Calendar.WEEK_OF_YEAR) == nowWeek
                }
                StatPeriodMode.YEAR_TO_DATE -> {
                    expYear == nowYear && exp.date <= System.currentTimeMillis()
                }
                StatPeriodMode.SPECIFIC_YEAR -> {
                    expYear == selectedYear
                }
                StatPeriodMode.SPECIFIC_MONTH -> {
                    if (selectedMonthIndex in 0..11) {
                        expYear == activeYearForMonth && expMonth == selectedMonthIndex
                    } else {
                        true
                    }
                }
            }
        }
    }

    // Berechne die Tagesanzahl des gewählten Zeitraums für präzise Durchschnittswerte
    val customRangeDays = remember(periodMode, selectedYear, selectedMonthIndex, activeYearForMonth, filteredExpenses) {
        val nowCal = Calendar.getInstance()
        when (periodMode) {
            StatPeriodMode.TODAY -> 1
            StatPeriodMode.THIS_WEEK -> 7
            StatPeriodMode.YEAR_TO_DATE -> max(1, nowCal.get(Calendar.DAY_OF_YEAR))
            StatPeriodMode.SPECIFIC_YEAR -> {
                if (selectedYear == nowCal.get(Calendar.YEAR)) {
                    max(1, nowCal.get(Calendar.DAY_OF_YEAR))
                } else {
                    val cal = Calendar.getInstance().apply { set(Calendar.YEAR, selectedYear) }
                    cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                }
            }
            StatPeriodMode.SPECIFIC_MONTH -> {
                if (selectedMonthIndex in 0..11) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, activeYearForMonth)
                        set(Calendar.MONTH, selectedMonthIndex)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                } else null
            }
            StatPeriodMode.ALL_TIME -> null
        }
    }

    val statistics = remember(filteredExpenses, categories, customRangeDays) {
        ExpenseStatistics.calculate(filteredExpenses, categories, customRangeDays)
    }

    // Erzeuge die Tages-Balken ("Balkendiagramm für jeden Tag") für den gewählten Zeitraum
    val dailyBarStats = remember(filteredExpenses, periodMode, selectedYear, selectedMonthIndex, activeYearForMonth, isEn) {
        buildDailyStatsForPeriod(
            expenses = filteredExpenses,
            periodMode = periodMode,
            selectedYear = selectedYear,
            selectedMonthIndex = selectedMonthIndex,
            activeYearForMonth = activeYearForMonth,
            isEn = isEn
        )
    }

    val periodDisplayLabel = remember(periodMode, selectedYear, selectedMonthIndex, activeYearForMonth, isEn) {
        when (periodMode) {
            StatPeriodMode.ALL_TIME -> if (isEn) "Total Since Recording Started" else "Gesamt seit Aufzeichnung"
            StatPeriodMode.TODAY -> if (isEn) "Today" else "Heutiger Tag"
            StatPeriodMode.THIS_WEEK -> if (isEn) "This Week" else "Heutige Woche"
            StatPeriodMode.YEAR_TO_DATE -> if (isEn) "Year to Date ($currentYear)" else "Jahr bis jetzt ($currentYear)"
            StatPeriodMode.SPECIFIC_YEAR -> if (isEn) "Year $selectedYear" else "Jahr $selectedYear"
            StatPeriodMode.SPECIFIC_MONTH -> {
                val mName = if (selectedMonthIndex in 0..11) monthNames[selectedMonthIndex] else ""
                "$mName $activeYearForMonth"
            }
        }
    }

    val currencyFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    // Smarte Finanz-Einblicke (Financial Insights & KPIs)
    val avgPerExpense = remember(filteredExpenses) {
        if (filteredExpenses.isNotEmpty()) filteredExpenses.sumOf { it.amount } / filteredExpenses.size else 0.0
    }

    val noSpendDays = remember(statistics.daysSpan, statistics.activeDaysCount) {
        max(0, statistics.daysSpan - statistics.activeDaysCount)
    }

    val noSpendPercentage = remember(statistics.daysSpan, noSpendDays) {
        if (statistics.daysSpan > 0) ((noSpendDays.toDouble() / statistics.daysSpan) * 100).toInt() else 0
    }

    val peakSpendingDay = remember(filteredExpenses, isEn) {
        if (filteredExpenses.isEmpty()) null
        else {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.GERMAN)
            val displayDateFormat = SimpleDateFormat("dd. MMM", if (isEn) Locale.US else Locale.GERMAN)
            val expensesByDay = filteredExpenses.groupBy { dateFormat.format(Date(it.date)) }
            val maxDayEntry = expensesByDay.maxByOrNull { it.value.sumOf { exp -> exp.amount } }
            maxDayEntry?.let { entry ->
                val firstDate = entry.value.first().date
                val daySum = entry.value.sumOf { it.amount }
                Pair(displayDateFormat.format(Date(firstDate)), daySum)
            }
        }
    }

    val lowestExpense = remember(filteredExpenses) {
        filteredExpenses.minByOrNull { it.amount }
    }

    val topCategory = remember(statistics.categoryStats) {
        statistics.categoryStats.firstOrNull()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER + ZEITRAUM-FILTER + MONATS-DROPDOWN
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AppStrings.navStatistics(currentLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Zeitraum-Chips: Gesamt seit Aufzeichnung, Heute, Diese Woche, Jahr bis jetzt (+ einzelne Jahre wenn > 1 Jahr Daten)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = periodMode == StatPeriodMode.ALL_TIME,
                            onClick = {
                                periodMode = StatPeriodMode.ALL_TIME
                                selectedMonthIndex = -1
                            },
                            label = {
                                Text(if (isEn) "Total Since Start" else "Gesamt seit Aufzeichnung")
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = periodMode == StatPeriodMode.TODAY,
                            onClick = {
                                periodMode = StatPeriodMode.TODAY
                                selectedMonthIndex = -1
                            },
                            label = {
                                Text(if (isEn) "Today" else "Heute")
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = periodMode == StatPeriodMode.THIS_WEEK,
                            onClick = {
                                periodMode = StatPeriodMode.THIS_WEEK
                                selectedMonthIndex = -1
                            },
                            label = {
                                Text(if (isEn) "This Week" else "Diese Woche")
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = periodMode == StatPeriodMode.YEAR_TO_DATE,
                            onClick = {
                                periodMode = StatPeriodMode.YEAR_TO_DATE
                                selectedYear = currentYear
                                selectedMonthIndex = -1
                            },
                            label = {
                                Text(if (isEn) "Year to Date" else "Jahr bis jetzt")
                            }
                        )
                    }

                    // Falls der Nutzer mehr als ein Jahr Daten gesammelt hat: auch die einzelnen Jahre anzeigen
                    if (availableYears.size > 1) {
                        items(availableYears) { year ->
                            FilterChip(
                                selected = periodMode == StatPeriodMode.SPECIFIC_YEAR && selectedYear == year,
                                onClick = {
                                    selectedYear = year
                                    periodMode = StatPeriodMode.SPECIFIC_YEAR
                                    selectedMonthIndex = -1
                                },
                                label = {
                                    Text(if (isEn) "Year $year" else "Jahr $year")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Monats-Dropdown-Menü (Alle 12 Monate wählbar)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isMonthDropdownExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (periodMode == StatPeriodMode.SPECIFIC_MONTH)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (periodMode == StatPeriodMode.SPECIFIC_MONTH)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (periodMode == StatPeriodMode.SPECIFIC_MONTH && selectedMonthIndex in 0..11) {
                                        if (isEn) "Selected Month: ${monthNames[selectedMonthIndex]} $activeYearForMonth"
                                        else "Gewählter Monat: ${monthNames[selectedMonthIndex]} $activeYearForMonth"
                                    } else {
                                        if (isEn) "Select Month (All Months)" else "Monat wählen (Alle Monate)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isMonthDropdownExpanded,
                        onDismissRequest = { isMonthDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isEn) "All Months (Overview)" else "Alle Monate (Gesamtübersicht)",
                                    fontWeight = if (selectedMonthIndex == -1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedMonthIndex = -1
                                if (periodMode == StatPeriodMode.SPECIFIC_MONTH) {
                                    periodMode = StatPeriodMode.ALL_TIME
                                }
                                isMonthDropdownExpanded = false
                            }
                        )
                        monthNames.forEachIndexed { index, monthName ->
                            val isSelectedMonth = periodMode == StatPeriodMode.SPECIFIC_MONTH && selectedMonthIndex == index
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$monthName $activeYearForMonth",
                                        fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedMonthIndex = index
                                    periodMode = StatPeriodMode.SPECIFIC_MONTH
                                    isMonthDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (statistics.expenseCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isEn) "No expenses for $periodDisplayLabel" else "Keine Ausgaben für: $periodDisplayLabel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) "Select another month/timeframe or add expenses."
                            else "Wähle oben einen anderen Monat bzw. Zeitraum oder füge Ausgaben hinzu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (allExpenses.isEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(onClick = { viewModel.seedDemoData() }) {
                                Text(AppStrings.loadSampleData(currentLanguage))
                            }
                        }
                    }
                }
            }
        } else {
            // 1. ÜBERSICHTS-DIAGRAMM (Kuchendiagramm <-> Balkendiagramm für jeden Tag)
            if (statistics.categoryStats.isNotEmpty()) {
                item {
                    CategoryPieChart(
                        categoryStats = statistics.categoryStats,
                        dailyStats = dailyBarStats,
                        periodSubtitle = periodDisplayLabel,
                        isEn = isEn
                    )
                }
            }

            // 2. DURCHSCHNITT & GESAMT (Reine Zahlen-Übersicht OHNE Diagramm)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isEn) "Averages & Total ($periodDisplayLabel)" else "Durchschnitt & Gesamt ($periodDisplayLabel)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Gesamt-Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEn) "Total ($periodDisplayLabel)" else "Gesamt ($periodDisplayLabel)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currencyFormat.format(statistics.totalAmount)} €",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "${statistics.expenseCount} ${if (isEn) "entries" else "Ausgaben"}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${statistics.activeDaysCount} / ${statistics.daysSpan} ${if (isEn) "active days" else "Tage aktiv"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // 2x2 Grid: Ø pro Tag, Ø pro Woche, Ø pro Monat, Ø pro Jahr (Ohne Diagramm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = if (isEn) "Ø per Day" else "Ø pro Tag",
                            value = statistics.dailyAverage,
                            subtitle = if (isEn) "over ${statistics.daysSpan} days" else "auf ${statistics.daysSpan} Tage",
                            icon = Icons.Default.CalendarToday,
                            accentColor = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isEn) "Ø per Week" else "Ø pro Woche",
                            value = statistics.weeklyAverage,
                            subtitle = if (isEn) "7-day average" else "7-Tage-Schnitt",
                            icon = Icons.Default.DateRange,
                            accentColor = Color(0xFF009688),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = if (isEn) "Ø per Month" else "Ø pro Monat",
                            value = statistics.monthlyAverage,
                            subtitle = if (isEn) "Monthly average" else "Monatsschnitt",
                            icon = Icons.Default.CalendarMonth,
                            accentColor = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isEn) "Ø per Year" else "Ø pro Jahr",
                            value = statistics.yearlyAverage,
                            subtitle = if (isEn) "Yearly average" else "Jahresschnitt",
                            icon = Icons.Default.Timeline,
                            accentColor = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. FINANZ-EINBLICKE & HIGHLIGHTS (Smart Finance Insights)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isEn) "Finance Insights & KPIs" else "Finanz-Einblicke & Kennzahlen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 2x2 Grid: Ausgabenfreie Tage, Spitzen-Tag, Ø pro Buchung, Top-Kategorie
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ausgabenfreie Tage (Budget-Disziplin & Sparerfolg)
                        FinanceInsightCard(
                            title = if (isEn) "No-Spend Days" else "Ausgabenfreie Tage",
                            value = "$noSpendDays ${if (isEn) "days" else "Tage"} ($noSpendPercentage%)",
                            subtitle = if (isEn) "out of ${statistics.daysSpan} days zero spend" else "von ${statistics.daysSpan} Tagen ohne Ausgabe",
                            icon = Icons.Default.Spa,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )

                        // Spitzen-Ausgabetag (Peak-Tag)
                        FinanceInsightCard(
                            title = if (isEn) "Peak Spending Day" else "Spitzen-Ausgabetag",
                            value = peakSpendingDay?.let { "${it.first}: ${currencyFormat.format(it.second)} €" } ?: "-",
                            subtitle = if (isEn) "Highest daily total" else "Höchster Tagesbetrag",
                            icon = Icons.Default.TrendingUp,
                            accentColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ø pro Buchung / Bon
                        FinanceInsightCard(
                            title = if (isEn) "Ø per Transaction" else "Ø pro Buchung",
                            value = "${currencyFormat.format(avgPerExpense)} €",
                            subtitle = if (isEn) "across ${statistics.expenseCount} entries" else "bei ${statistics.expenseCount} Ausgaben",
                            icon = Icons.Default.ShoppingCart,
                            accentColor = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )

                        // Größter Ausgabenbereich (Top Category)
                        FinanceInsightCard(
                            title = if (isEn) "Top Category" else "Größter Posten",
                            value = topCategory?.let { "${it.category.localizedName(isEn)} (${String.format(Locale.GERMAN, "%.1f", it.percentage)}%)" } ?: "-",
                            subtitle = topCategory?.let { "${currencyFormat.format(it.totalAmount)} € gesamt" } ?: "-",
                            icon = Icons.Default.Star,
                            accentColor = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weekday spending chart (Mo-So)
            if (statistics.weekdayStats.isNotEmpty()) {
                item {
                    WeekdaySpendingChart(
                        weekdayStats = statistics.weekdayStats,
                        isEn = isEn
                    )
                }
            }

            // Payment methods breakdown
            if (statistics.paymentMethodStats.isNotEmpty()) {
                item {
                    PaymentMethodsCard(
                        paymentStats = statistics.paymentMethodStats,
                        isEn = isEn
                    )
                }
            }

            // Spannbreite der Ausgaben: Höchste & Günstigste Ausgabe
            if (statistics.highestExpense != null) {
                val highExp = statistics.highestExpense
                val highCategory = categories.firstOrNull { it.id == highExp.categoryId }
                val highDate = SimpleDateFormat("dd. MMM yyyy", if (isEn) Locale.US else Locale.GERMAN).format(Date(highExp.date))

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isEn) "Expense Range: Highest & Lowest" else "Spannbreite der Ausgaben",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Höchste Einzelausgabe
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) "Highest Single Expense" else "Höchste Einzelausgabe",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = highExp.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val highCatName = highCategory?.localizedName(isEn)
                                        Text(
                                            text = if (highCatName != null) "$highDate • $highCatName" else highDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Text(
                                        text = "${currencyFormat.format(highExp.amount)} €",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }

                                // Kleinste Einzelausgabe (falls abweichend)
                                if (lowestExpense != null && lowestExpense.id != highExp.id) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )

                                    val lowCategory = categories.firstOrNull { it.id == lowestExpense.categoryId }
                                    val lowDate = SimpleDateFormat("dd. MMM yyyy", if (isEn) Locale.US else Locale.GERMAN).format(Date(lowestExpense.date))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isEn) "Lowest Single Expense" else "Kleinste Einzelausgabe",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = lowestExpense.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val lowCatName = lowCategory?.localizedName(isEn)
                                            Text(
                                                text = if (lowCatName != null) "$lowDate • $lowCatName" else lowDate,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Text(
                                            text = "${currencyFormat.format(lowestExpense.amount)} €",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category list breakdown
            if (statistics.categoryStats.isNotEmpty()) {
                item {
                    Text(
                        text = if (isEn) "Category Details" else "Kategorien-Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(statistics.categoryStats) { catStat ->
                    CategoryStatItem(
                        catStat = catStat,
                        currencyFormat = currencyFormat,
                        isEn = isEn
                    )
                }
            }
        }
    }
}

private fun buildDailyStatsForPeriod(
    expenses: List<Expense>,
    periodMode: StatPeriodMode,
    selectedYear: Int,
    selectedMonthIndex: Int,
    activeYearForMonth: Int,
    isEn: Boolean
): List<DayStat> {
    if (expenses.isEmpty()) return emptyList()

    val locale = if (isEn) Locale.US else Locale.GERMAN
    val dayKeyFormat = SimpleDateFormat("yyyyMMdd", locale)
    val shortDateFormat = SimpleDateFormat("dd.MM.", locale)
    val dayOfWeekFormat = SimpleDateFormat("EEE", locale)

    val expensesByDayKey = expenses.groupBy { dayKeyFormat.format(Date(it.date)) }

    val startCal = Calendar.getInstance()
    val endCal = Calendar.getInstance()

    when (periodMode) {
        StatPeriodMode.TODAY -> {
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            endCal.timeInMillis = startCal.timeInMillis
        }
        StatPeriodMode.THIS_WEEK -> {
            startCal.firstDayOfWeek = Calendar.MONDAY
            startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            endCal.timeInMillis = startCal.timeInMillis
            endCal.add(Calendar.DAY_OF_YEAR, 6)
        }
        StatPeriodMode.SPECIFIC_MONTH -> {
            val mIdx = if (selectedMonthIndex in 0..11) selectedMonthIndex else startCal.get(Calendar.MONTH)
            startCal.set(Calendar.YEAR, activeYearForMonth)
            startCal.set(Calendar.MONTH, mIdx)
            startCal.set(Calendar.DAY_OF_MONTH, 1)
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            startCal.set(Calendar.MILLISECOND, 0)

            val maxDay = startCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            endCal.timeInMillis = startCal.timeInMillis
            endCal.set(Calendar.DAY_OF_MONTH, maxDay)
        }
        StatPeriodMode.YEAR_TO_DATE,
        StatPeriodMode.SPECIFIC_YEAR,
        StatPeriodMode.ALL_TIME -> {
            val minDate = expenses.minOf { it.date }
            val maxDate = max(expenses.maxOf { it.date }, System.currentTimeMillis())
            startCal.timeInMillis = minDate
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            startCal.set(Calendar.MILLISECOND, 0)

            endCal.timeInMillis = maxDate
            endCal.set(Calendar.HOUR_OF_DAY, 0)
            endCal.set(Calendar.MINUTE, 0)
            endCal.set(Calendar.SECOND, 0)
            endCal.set(Calendar.MILLISECOND, 0)
        }
    }

    val result = ArrayList<DayStat>()
    val iterCal = Calendar.getInstance().apply { timeInMillis = startCal.timeInMillis }
    var safetyCounter = 0

    while (!iterCal.after(endCal) && safetyCounter < 730) {
        val key = dayKeyFormat.format(iterCal.time)
        val dayTotal = expensesByDayKey[key]?.sumOf { it.amount } ?: 0.0
        result.add(
            DayStat(
                dateLabel = shortDateFormat.format(iterCal.time),
                dayOfWeek = dayOfWeekFormat.format(iterCal.time),
                timestamp = iterCal.timeInMillis,
                totalAmount = dayTotal
            )
        )
        iterCal.add(Calendar.DAY_OF_YEAR, 1)
        safetyCounter++
    }

    return result
}

@Composable
fun CategoryStatItem(
    catStat: CategoryStat,
    currencyFormat: NumberFormat,
    isEn: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(category = catStat.category, size = 38)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = catStat.category.localizedName(isEn),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${catStat.count} ${if (isEn) "entries" else "Einträge"} • Ø ${currencyFormat.format(catStat.averagePerExpense)} €",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${currencyFormat.format(catStat.totalAmount)} €",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(if (isEn) Locale.US else Locale.GERMAN, "%.1f %%", catStat.percentage),
                        style = MaterialTheme.typography.labelSmall,
                        color = catStat.category.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (catStat.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = catStat.category.color,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun FinanceInsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}
