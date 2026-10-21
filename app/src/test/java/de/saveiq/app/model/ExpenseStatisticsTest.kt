package de.saveiq.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExpenseStatisticsTest {

    private val categories = ExpenseCategory.defaultCategories

    @Test
    fun testEmptyStatistics() {
        val stats = ExpenseStatistics.calculate(emptyList(), categories)
        assertEquals(0.0, stats.totalAmount, 0.001)
        assertEquals(0, stats.expenseCount)
        assertEquals(0.0, stats.dailyAverage, 0.001)
        assertEquals(0.0, stats.weeklyAverage, 0.001)
        assertEquals(0.0, stats.monthlyAverage, 0.001)
        assertEquals(0.0, stats.yearlyAverage, 0.001)
        assertTrue(stats.categoryStats.isEmpty())
    }

    @Test
    fun testStatisticsCalculation() {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        val calYesterday = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = calYesterday.timeInMillis

        val calTwoDaysAgo = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -2) }
        val twoDaysAgo = calTwoDaysAgo.timeInMillis

        val expenses = listOf(
            Expense(
                id = "1",
                title = "Hotel",
                amount = 200.0,
                categoryId = "cat_unterkunft",
                date = twoDaysAgo
            ),
            Expense(
                id = "2",
                title = "Dinner",
                amount = 50.0,
                categoryId = "cat_verpflegung",
                date = yesterday
            ),
            Expense(
                id = "3",
                title = "Museum",
                amount = 50.0,
                categoryId = "cat_aktivitaeten",
                date = now
            )
        )

        val stats = ExpenseStatistics.calculate(expenses, categories)

        // Total
        assertEquals(300.0, stats.totalAmount, 0.001)
        assertEquals(3, stats.expenseCount)

        // Days span = from 2 days ago to today = 3 days
        assertEquals(3, stats.daysSpan)
        assertEquals(3, stats.activeDaysCount)

        // Daily average: 300 / 3 = 100.0
        assertEquals(100.0, stats.dailyAverage, 0.001)

        // Weekly average: 300 / (3 / 7) = 300 / 1.0 (since min weeks is 1.0) = 300.0
        assertTrue(stats.weeklyAverage > 0)

        // Monthly & Yearly averages
        assertTrue(stats.monthlyAverage > 0)
        assertTrue(stats.yearlyAverage > 0)

        // Highest expense
        assertNotNull(stats.highestExpense)
        assertEquals("Hotel", stats.highestExpense?.title)
        assertEquals(200.0, stats.highestExpense?.amount ?: 0.0, 0.001)

        // Categories check
        assertEquals(3, stats.categoryStats.size)
        val hotelStat = stats.categoryStats.find { it.category.id == "cat_unterkunft" }
        assertNotNull(hotelStat)
        assertEquals(200.0, hotelStat!!.totalAmount, 0.001)
        assertEquals(66.666f, hotelStat.percentage, 0.5f)
    }
}
