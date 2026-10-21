package de.saveiq.app.model

import java.util.UUID

enum class RecurringInterval(val code: String, val labelDe: String, val labelEn: String) {
    DAILY("DAILY", "Täglich", "Daily"),
    WEEKLY("WEEKLY", "Wöchentlich", "Weekly"),
    MONTHLY("MONTHLY", "Monatlich", "Monthly"),
    YEARLY("YEARLY", "Jährlich", "Yearly");

    fun label(isEn: Boolean): String = if (isEn) labelEn else labelDe

    companion object {
        fun fromCode(code: String): RecurringInterval =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: MONTHLY
    }
}

data class RecurringExpense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val currency: String = "EUR",
    val categoryId: String,
    val interval: RecurringInterval = RecurringInterval.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val lastExecutionDate: Long = 0L,
    val nextDueDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Dauerauftrag",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val notes: String = "",
    val isActive: Boolean = true
)
