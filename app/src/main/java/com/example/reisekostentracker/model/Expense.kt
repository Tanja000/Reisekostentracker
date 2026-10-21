package com.example.reisekostentracker.model

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val currency: String = "EUR",
    val categoryId: String,
    val tripId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val paymentMethod: String = "Karte",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val feelingEmoji: String? = null
)
