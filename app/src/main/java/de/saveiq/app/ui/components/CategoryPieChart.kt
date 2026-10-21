package de.saveiq.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.saveiq.app.model.CategoryStat
import java.text.NumberFormat
import java.util.Locale

enum class ChartMetricScope(val label: String, val shortLabel: String) {
    TOTAL("Gesamt", "Gesamt"),
    DAY("Pro Tag", "Ø Tag"),
    WEEK("Pro Woche", "Ø Woche"),
    MONTH("Pro Monat", "Ø Monat"),
    YEAR("Pro Jahr", "Ø Jahr");

    fun label(isEn: Boolean): String = if (isEn) {
        when (this) {
            TOTAL -> "Total"
            DAY -> "Per Day"
            WEEK -> "Per Week"
            MONTH -> "Per Month"
            YEAR -> "Per Year"
        }
    } else label

    fun shortLabel(isEn: Boolean): String = if (isEn) {
        when (this) {
            TOTAL -> "Total"
            DAY -> "Ø Day"
            WEEK -> "Ø Week"
            MONTH -> "Ø Month"
            YEAR -> "Ø Year"
        }
    } else shortLabel
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPieChart(
    categoryStats: List<CategoryStat>,
    selectedScope: ChartMetricScope,
    onScopeSelected: (ChartMetricScope) -> Unit,
    modifier: Modifier = Modifier,
    isEn: Boolean = false
) {
    val currencyFormat = remember {
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val totalAmount = categoryStats.sumOf {
        when (selectedScope) {
            ChartMetricScope.TOTAL -> it.totalAmount
            ChartMetricScope.DAY -> it.dailyShare
            ChartMetricScope.WEEK -> it.weeklyShare
            ChartMetricScope.MONTH -> it.monthlyShare
            ChartMetricScope.YEAR -> it.yearlyShare
        }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedScope, categoryStats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = if (isEn) "Pie Chart: Category Breakdown" else "Kuchendiagramm: Kategorien-Verteilung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEn) "Choose a timeframe to view cost distribution:" else "Wähle eine Ansicht, um die Verteilung nach Zeitraum zu berechnen:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switcher Buttons: Pro Tag, Pro Woche, Pro Monat, Pro Jahr, Gesamt
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ChartMetricScope.values().forEach { scope ->
                    FilterChip(
                        selected = selectedScope == scope,
                        onClick = { onScopeSelected(scope) },
                        label = { Text(scope.label(isEn)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedScope == scope,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Donut Chart Canvas with Center Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 32.dp.toPx()
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
                            val value = when (selectedScope) {
                                ChartMetricScope.TOTAL -> stat.totalAmount
                                ChartMetricScope.DAY -> stat.dailyShare
                                ChartMetricScope.WEEK -> stat.weeklyShare
                                ChartMetricScope.MONTH -> stat.monthlyShare
                                ChartMetricScope.YEAR -> stat.yearlyShare
                            }
                            val sweepAngle = ((value / totalAmount) * 360f).toFloat() * animationProgress.value
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
                        text = selectedScope.shortLabel(isEn),
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
                    val value = when (selectedScope) {
                        ChartMetricScope.TOTAL -> stat.totalAmount
                        ChartMetricScope.DAY -> stat.dailyShare
                        ChartMetricScope.WEEK -> stat.weeklyShare
                        ChartMetricScope.MONTH -> stat.monthlyShare
                        ChartMetricScope.YEAR -> stat.yearlyShare
                    }
                    val pct = if (totalAmount > 0) (value / totalAmount * 100f) else 0f

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(stat.category.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stat.category.localizedName(isEn)}: ${currencyFormat.format(value)} € (${String.format(Locale.GERMAN, "%.1f", pct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
