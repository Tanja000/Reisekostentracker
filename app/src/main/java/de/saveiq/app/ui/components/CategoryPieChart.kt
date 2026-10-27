package de.saveiq.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.saveiq.app.model.CategoryStat
import de.saveiq.app.model.DayStat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

enum class ChartMetricScope(val label: String, val shortLabel: String) {
    TOTAL("Gesamt", "Gesamt"),
    DAY("Pro Tag", "Ø Tag"),
    WEEK("Pro Woche", "Ø Woche"),
    MONTH("Pro Monat", "Ø Monat"),
    YEAR("Pro Jahr", "Ø Jahr")
}

enum class OverviewChartType {
    PIE,
    DAILY_BAR
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPieChart(
    categoryStats: List<CategoryStat>,
    dailyStats: List<DayStat>,
    periodSubtitle: String,
    modifier: Modifier = Modifier,
    isEn: Boolean = false
) {
    var chartType by rememberSaveable { mutableStateOf(OverviewChartType.PIE) }

    val currencyFormat = remember(isEn) {
        NumberFormat.getNumberInstance(if (isEn) Locale.US else Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val totalAmount = remember(categoryStats) {
        categoryStats.sumOf { it.totalAmount }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categoryStats, chartType) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(650))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Header & Subtitle
            Text(
                text = if (chartType == OverviewChartType.PIE) {
                    if (isEn) "Category Breakdown" else "Kategorien-Übersicht"
                } else {
                    if (isEn) "Daily Expenses" else "Tagesausgaben im Verlauf"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = periodSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Toggle: Kuchen <-> Balken
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
                    val isPie = chartType == OverviewChartType.PIE
                    val pieBg by animateColorAsState(
                        if (isPie) MaterialTheme.colorScheme.primary else Color.Transparent,
                        label = "pieBg"
                    )
                    val pieText by animateColorAsState(
                        if (isPie) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        label = "pieText"
                    )

                    val isBar = chartType == OverviewChartType.DAILY_BAR
                    val barBg by animateColorAsState(
                        if (isBar) MaterialTheme.colorScheme.primary else Color.Transparent,
                        label = "barBg"
                    )
                    val barText by animateColorAsState(
                        if (isBar) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        label = "barText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(pieBg)
                            .clickable { chartType = OverviewChartType.PIE }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = pieText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Pie" else "Kuchen",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isPie) FontWeight.Bold else FontWeight.Medium,
                                color = pieText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(barBg)
                            .clickable { chartType = OverviewChartType.DAILY_BAR }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = barText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Bars" else "Balken",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isBar) FontWeight.Bold else FontWeight.Medium,
                                color = barText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = chartType,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "chartTypeSwitch"
            ) { currentType ->
                if (currentType == OverviewChartType.PIE) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Donut Chart Canvas with Center Text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(185.dp)) {
                                val strokeWidth = 30.dp.toPx()
                                val arcSize = size.width - strokeWidth
                                var startAngle = -90f

                                if (categoryStats.isEmpty() || totalAmount <= 0.0) {
                                    drawCircle(
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        radius = arcSize / 2,
                                        style = Stroke(width = strokeWidth)
                                    )
                                } else {
                                    categoryStats.forEach { stat ->
                                        val sweepAngle =
                                            ((stat.totalAmount / totalAmount) * 360f).toFloat() * animationProgress.value
                                        drawArc(
                                            color = stat.category.color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                            size = Size(arcSize, arcSize),
                                            style = Stroke(width = strokeWidth)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                            }

                            // Center Information
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isEn) "Overview" else "Übersicht",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currencyFormat.format(totalAmount)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categoryStats.forEach { stat ->
                                val pct = if (totalAmount > 0) (stat.totalAmount / totalAmount * 100f) else 0f

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(stat.category.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${stat.category.localizedName(isEn)}: ${currencyFormat.format(stat.totalAmount)} € (${String.format(if (isEn) Locale.US else Locale.GERMAN, "%.1f", pct)}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Daily Bar Chart for EVERY day in the selected period
                    DailyExpensesBarView(
                        dailyStats = dailyStats,
                        isEn = isEn
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyExpensesBarView(
    dailyStats: List<DayStat>,
    isEn: Boolean
) {
    val maxAmount = max(1.0, dailyStats.maxOfOrNull { it.totalAmount } ?: 1.0)
    val numberFormat = remember(isEn) {
        NumberFormat.getIntegerInstance(if (isEn) Locale.US else Locale.GERMANY)
    }
    val scrollState = rememberScrollState()

    // Scroll to the right (most recent days) when there are many days
    LaunchedEffect(dailyStats.size) {
        if (dailyStats.size > 10) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (dailyStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEn) "No daily entries for this period" else "Keine Tageseinträge im gewählten Zeitraum",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            val useHorizontalScroll = dailyStats.size > 8
            val rowModifier = if (useHorizontalScroll) {
                Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .horizontalScroll(scrollState)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            }

            Row(
                modifier = rowModifier,
                horizontalArrangement = if (useHorizontalScroll) Arrangement.spacedBy(8.dp) else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEach { day ->
                    val fraction = (day.totalAmount / maxAmount).toFloat().coerceIn(0.04f, 1f)
                    val animatedHeight by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 500),
                        label = "dailyBarHeight"
                    )

                    val colModifier = if (useHorizontalScroll) {
                        Modifier
                            .width(42.dp)
                            .fillMaxHeight()
                    } else {
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    }

                    Column(
                        modifier = colModifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (day.totalAmount > 0) {
                            Text(
                                text = "${numberFormat.format(day.totalAmount)}€",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "0€",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .height((110 * animatedHeight).dp)
                                .width(if (useHorizontalScroll) 22.dp else 24.dp)
                                .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                .background(
                                    if (day.totalAmount > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val displayDay = if (isEn) {
                            when (day.dayOfWeek.trim().removeSuffix(".")) {
                                "Mo" -> "Mon"
                                "Di" -> "Tue"
                                "Mi" -> "Wed"
                                "Do" -> "Thu"
                                "Fr" -> "Fri"
                                "Sa" -> "Sat"
                                "So" -> "Sun"
                                else -> day.dayOfWeek
                            }
                        } else day.dayOfWeek.removeSuffix(".")

                        Text(
                            text = displayDay,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = day.dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            if (dailyStats.size > 8) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEn) "↔ Swipe horizontally to view all ${dailyStats.size} days"
                    else "↔ Horizontal wischen, um alle ${dailyStats.size} Tage zu sehen",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
