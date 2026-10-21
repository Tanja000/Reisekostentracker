package de.saveiq.app.model

data class Trip(
    val id: String,
    val name: String,
    val destination: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val budget: Double? = null,
    val currency: String = "EUR",
    val isDefault: Boolean = false
) {
    val durationDays: Int
        get() {
            val diff = endDate - startDate
            return ((diff / (1000L * 60 * 60 * 24)) + 1).coerceAtLeast(1).toInt()
        }
}
