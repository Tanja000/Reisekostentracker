package com.example.reisekostentracker.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class CategoryStat(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float, // 0..100
    val count: Int,
    val averagePerExpense: Double,
    val dailyShare: Double = 0.0,
    val weeklyShare: Double = 0.0,
    val monthlyShare: Double = 0.0,
    val yearlyShare: Double = 0.0
)

data class DayStat(
    val dateLabel: String,
    val dayOfWeek: String,
    val timestamp: Long,
    val totalAmount: Double
)

data class MonthStat(
    val monthLabel: String,
    val yearMonthKey: String,
    val totalAmount: Double,
    val count: Int
)

data class PaymentMethodStat(
    val method: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class WeekdayStat(
    val dayIndex: Int, // 1=Mo, 7=So
    val dayName: String,
    val totalAmount: Double,
    val percentage: Float
)

data class ExpenseStatistics(
    val totalAmount: Double,
    val expenseCount: Int,
    val daysSpan: Int,
    val activeDaysCount: Int,
    val dailyAverage: Double,
    val weeklyAverage: Double,
    val monthlyAverage: Double,
    val yearlyAverage: Double,
    val highestExpense: Expense?,
    val categoryStats: List<CategoryStat>,
    val recentDaysStats: List<DayStat>,
    val monthlyStats: List<MonthStat>,
    val paymentMethodStats: List<PaymentMethodStat>,
    val weekdayStats: List<WeekdayStat>
) {
    companion object {
        private val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.GERMAN)
        private val dayLabelFormat = SimpleDateFormat("dd. MMM", Locale.GERMAN)
        private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.GERMAN)
        private val monthKeyFormat = SimpleDateFormat("yyyyMM", Locale.GERMAN)
        private val monthLabelFormat = SimpleDateFormat("MMM yyyy", Locale.GERMAN)

        fun empty(): ExpenseStatistics {
            return ExpenseStatistics(
                totalAmount = 0.0,
                expenseCount = 0,
                daysSpan = 1,
                activeDaysCount = 0,
                dailyAverage = 0.0,
                weeklyAverage = 0.0,
                monthlyAverage = 0.0,
                yearlyAverage = 0.0,
                highestExpense = null,
                categoryStats = emptyList(),
                recentDaysStats = emptyList(),
                monthlyStats = emptyList(),
                paymentMethodStats = emptyList(),
                weekdayStats = emptyList()
            )
        }

        fun calculate(
            expenses: List<Expense>,
            categories: List<ExpenseCategory>,
            customDateRangeDays: Int? = null
        ): ExpenseStatistics {
            if (expenses.isEmpty()) {
                return empty()
            }

            val totalAmount = expenses.sumOf { it.amount }
            val expenseCount = expenses.size
            val highestExpense = expenses.maxByOrNull { it.amount }

            // Calculate date span
            val minDate = expenses.minOf { it.date }
            val maxDate = expenses.maxOf { it.date }
            val diffMillis = maxDate - minDate
            val calculatedDays = (diffMillis / (1000L * 60 * 60 * 24)).toInt() + 1
            val daysSpan = customDateRangeDays ?: max(1, calculatedDays)

            val distinctDays = expenses.map { dayKeyFormat.format(Date(it.date)) }.toSet()
            val activeDaysCount = distinctDays.size

            val distinctMonths = expenses.map { monthKeyFormat.format(Date(it.date)) }.toSet()
            val distinctYears = expenses.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.YEAR)
            }.toSet()

            val dailyAverage = if (daysSpan > 0) totalAmount / daysSpan else 0.0
            val weeksSpan = max(1.0, daysSpan / 7.0)
            val weeklyAverage = totalAmount / weeksSpan
            val monthsSpan = max(1.0, max(distinctMonths.size.toDouble(), daysSpan / 30.4375))
            val monthlyAverage = totalAmount / monthsSpan
            val yearsSpan = max(1.0, max(distinctYears.size.toDouble(), daysSpan / 365.25))
            val yearlyAverage = totalAmount / yearsSpan

            // Category Stats with time breakdowns
            val categoryMap = categories.associateBy { it.id }
            val expensesByCategory = expenses.groupBy { it.categoryId }
            val categoryStats = expensesByCategory.map { (catId, catExpenses) ->
                val cat = categoryMap[catId] ?: ExpenseCategory(
                    id = catId,
                    name = "Unbekannt",
                    iconName = "category",
                    colorValue = 0xFF757575L
                )
                val catTotal = catExpenses.sumOf { it.amount }
                val percentage = if (totalAmount > 0) ((catTotal / totalAmount) * 100).toFloat() else 0f
                CategoryStat(
                    category = cat,
                    totalAmount = catTotal,
                    percentage = percentage,
                    count = catExpenses.size,
                    averagePerExpense = catTotal / catExpenses.size,
                    dailyShare = catTotal / daysSpan,
                    weeklyShare = catTotal / weeksSpan,
                    monthlyShare = catTotal / monthsSpan,
                    yearlyShare = catTotal / yearsSpan
                )
            }.sortedByDescending { it.totalAmount }

            // Recent 7 Days Chart Data
            val recentDaysStats = ArrayList<DayStat>()
            val anchorDate = max(System.currentTimeMillis(), maxDate)
            for (i in 6 downTo 0) {
                val dayCal = Calendar.getInstance().apply {
                    timeInMillis = anchorDate
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                val key = dayKeyFormat.format(dayCal.time)
                val dayExpenses = expenses.filter { dayKeyFormat.format(Date(it.date)) == key }
                val dayTotal = dayExpenses.sumOf { it.amount }
                recentDaysStats.add(
                    DayStat(
                        dateLabel = dayLabelFormat.format(dayCal.time),
                        dayOfWeek = dayOfWeekFormat.format(dayCal.time),
                        timestamp = dayCal.timeInMillis,
                        totalAmount = dayTotal
                    )
                )
            }

            // Monthly breakdown
            val monthlyStats = expenses.groupBy { monthKeyFormat.format(Date(it.date)) }
                .map { (monthKey, monthExpenses) ->
                    val sampleDate = Date(monthExpenses.first().date)
                    MonthStat(
                        monthLabel = monthLabelFormat.format(sampleDate),
                        yearMonthKey = monthKey,
                        totalAmount = monthExpenses.sumOf { it.amount },
                        count = monthExpenses.size
                    )
                }.sortedByDescending { it.yearMonthKey }

            // Payment method breakdown
            val paymentMethodStats = expenses.groupBy { it.paymentMethod }
                .map { (method, methodExpenses) ->
                    val sum = methodExpenses.sumOf { it.amount }
                    val pct = if (totalAmount > 0) ((sum / totalAmount) * 100).toFloat() else 0f
                    PaymentMethodStat(
                        method = method,
                        totalAmount = sum,
                        percentage = pct,
                        count = methodExpenses.size
                    )
                }.sortedByDescending { it.totalAmount }

            // Weekday breakdown (Mo-So)
            val weekdayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
            val weekdayCal = Calendar.getInstance()
            val expensesByWeekday = expenses.groupBy {
                weekdayCal.timeInMillis = it.date
                // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ... Saturday=7
                val dow = weekdayCal.get(Calendar.DAY_OF_WEEK)
                if (dow == Calendar.SUNDAY) 6 else dow - 2
            }
            val weekdayStats = (0..6).map { idx ->
                val dayExpenses = expensesByWeekday[idx] ?: emptyList()
                val sum = dayExpenses.sumOf { it.amount }
                val pct = if (totalAmount > 0) ((sum / totalAmount) * 100).toFloat() else 0f
                WeekdayStat(
                    dayIndex = idx,
                    dayName = weekdayNames[idx],
                    totalAmount = sum,
                    percentage = pct
                )
            }

            return ExpenseStatistics(
                totalAmount = totalAmount,
                expenseCount = expenseCount,
                daysSpan = daysSpan,
                activeDaysCount = activeDaysCount,
                dailyAverage = dailyAverage,
                weeklyAverage = weeklyAverage,
                monthlyAverage = monthlyAverage,
                yearlyAverage = yearlyAverage,
                highestExpense = highestExpense,
                categoryStats = categoryStats,
                recentDaysStats = recentDaysStats,
                monthlyStats = monthlyStats,
                paymentMethodStats = paymentMethodStats,
                weekdayStats = weekdayStats
            )
        }
    }
}
