package com.example.reisekostentracker.data

import android.content.Context
import com.example.reisekostentracker.model.Expense
import com.example.reisekostentracker.model.ExpenseCategory
import com.example.reisekostentracker.model.Trip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class ExpenseRepository(context: Context) {
    private val dbHelper = TravelExpenseDbHelper(context)
    private val prefs = context.getSharedPreferences("rk_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _categories = MutableStateFlow<List<ExpenseCategory>>(emptyList())
    val categories: StateFlow<List<ExpenseCategory>> = _categories.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    init {
        refreshAll()
    }

    private fun insertInitialDemoData() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        fun daysAgo(days: Int, hour: Int = 12): Long {
            return (cal.clone() as Calendar).apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -days)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 30)
            }.timeInMillis
        }

        val demoList = listOf(
            Expense(UUID.randomUUID().toString(), "Wocheneinkauf REWE", 68.40, "EUR", "cat_lebensmittel", null, daysAgo(0, 11), "Obst, Gemüse, Milch & Vorrat", "Karte", 48.1375, 11.5750, "München Marienplatz", feelingEmoji = null),
            Expense(UUID.randomUUID().toString(), "Mittagessen Trattoria", 14.50, "EUR", "cat_gastronomie", null, daysAgo(0, 13), "Pizza & Espresso", "Bar", 48.1360, 11.5770, "Trattoria Centro", feelingEmoji = "😊"),
            Expense(UUID.randomUUID().toString(), "Miete & Nebenkosten", 850.00, "EUR", "cat_wohnen", null, daysAgo(1, 8), "Monatsüberweisung", "Online", 48.1400, 11.5650, "Wohnung", feelingEmoji = null),
            Expense(UUID.randomUUID().toString(), "Tanken Aral Super E10", 56.80, "EUR", "cat_transport", null, daysAgo(2, 17), "42 Liter Benzin", "Karte", 48.1650, 11.5950, "Aral Tankstelle", feelingEmoji = "💸"),
            Expense(UUID.randomUUID().toString(), "Fitnessstudio Monatsbeitrag", 35.00, "EUR", "cat_abos", null, daysAgo(3, 9), "FitX Mitgliedschaft", "Online", 48.1300, 11.5900, "Gym Studio", feelingEmoji = "😊"),
            Expense(UUID.randomUUID().toString(), "Netflix & Spotify Abos", 22.98, "EUR", "cat_abos", null, daysAgo(4, 10), "Streaming Dienste", "Online", 48.1400, 11.5700, "Online", feelingEmoji = "🥱"),
            Expense(UUID.randomUUID().toString(), "Neue Sneaker H&M", 79.95, "EUR", "cat_shopping", null, daysAgo(5, 15), "Schuhe & Shirt", "Karte", 48.1380, 11.5730, "Kaufingerstraße", feelingEmoji = "⚡"),
            Expense(UUID.randomUUID().toString(), "Kino & Popcorn am Abend", 24.50, "EUR", "cat_freizeit", null, daysAgo(7, 20), "Cinema Preview", "Karte", 48.1450, 11.5620, "Filmpalast", feelingEmoji = "🥳"),
            Expense(UUID.randomUUID().toString(), "Apotheke Schmerzmittel", 12.80, "EUR", "cat_gesundheit", null, daysAgo(10, 11), "Ibuprofen & Pflaster", "Bar", 48.1360, 11.5760, "Apotheke am Markt", feelingEmoji = "🤔"),
            Expense(UUID.randomUUID().toString(), "Wochenmarkt frisches Obst", 15.20, "EUR", "cat_lebensmittel", null, daysAgo(12, 10), "Äpfel, Beeren & Nüsse", "Bar", 48.1351, 11.5761, "Viktualienmarkt", feelingEmoji = null),
            Expense(UUID.randomUUID().toString(), "Stromabschlag Stadtwerke", 65.00, "EUR", "cat_wohnen", null, daysAgo(15, 8), "Monatlicher Abschlag", "Online", 48.1370, 11.5780, "Stadtwerke München", feelingEmoji = null),
            Expense(UUID.randomUUID().toString(), "Kaffee & Croissant Bäckerei", 4.80, "EUR", "cat_gastronomie", null, daysAgo(18, 9), "Frühstück to go", "Bar", 48.1370, 11.5750, "Bäckerei Müller", feelingEmoji = "☕"),
            Expense(UUID.randomUUID().toString(), "Online Frust-Bestellung Deko", 42.00, "EUR", "cat_shopping", null, daysAgo(6, 22), "Spontankauf nachts", "Online", 48.1390, 11.5740, "Online Store", feelingEmoji = "🤦")
        )
        demoList.forEach { dbHelper.insertExpense(it) }
    }

    private fun refreshAll() {
        scope.launch {
            _categories.value = dbHelper.getAllCategories()
            _trips.value = dbHelper.getAllTrips()

            val hasSeeded = prefs.getBoolean("has_seeded_initial", false)
            if (!hasSeeded) {
                val existing = dbHelper.getAllExpenses()
                if (existing.isEmpty()) {
                    insertInitialDemoData()
                }
                prefs.edit().putBoolean("has_seeded_initial", true).apply()
            }

            val rawExpenses = dbHelper.getAllExpenses()

            val sampleSpots = listOf(
                Triple(41.9028, 12.4964, "Rom Zentrum"),
                Triple(41.8887, 12.4705, "Trastevere, Rom"),
                Triple(41.8902, 12.4922, "Kolosseum, Rom"),
                Triple(43.7710, 11.2486, "Florenz Altstadt"),
                Triple(43.7677, 11.2553, "Uffizien, Florenz"),
                Triple(43.4678, 11.0435, "San Gimignano, Toskana"),
                Triple(43.3188, 11.3307, "Siena, Toskana"),
                Triple(45.4342, 12.3388, "Venedig"),
                Triple(45.4642, 9.1900, "Mailand"),
                Triple(48.1371, 11.5754, "München"),
                Triple(41.3855, 2.1820, "Barcelona")
            )

            val resolved = rawExpenses.mapIndexed { idx, exp ->
                if (exp.latitude == null || exp.longitude == null) {
                    val spot = sampleSpots[idx % sampleSpots.size]
                    val jitterLat = (Math.random() - 0.5) * 0.03
                    val jitterLng = (Math.random() - 0.5) * 0.03
                    val fixed = exp.copy(
                        latitude = spot.first + jitterLat,
                        longitude = spot.second + jitterLng,
                        locationName = exp.locationName ?: spot.third
                    )
                    dbHelper.updateExpense(fixed)
                    fixed
                } else exp
            }

            _expenses.value = resolved
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        currency: String = "EUR",
        categoryId: String,
        tripId: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String = "",
        paymentMethod: String = "Karte",
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null,
        feelingEmoji: String? = null
    ) {
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            title = title,
            amount = amount,
            currency = currency,
            categoryId = categoryId,
            tripId = tripId,
            date = date,
            notes = notes,
            paymentMethod = paymentMethod,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            feelingEmoji = feelingEmoji
        )
        scope.launch {
            dbHelper.insertExpense(expense)
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun updateExpense(expense: Expense) {
        scope.launch {
            dbHelper.updateExpense(expense)
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun deleteExpense(id: String) {
        scope.launch {
            dbHelper.deleteExpense(id)
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun addCategory(name: String, iconName: String, colorValue: Long) {
        val cat = ExpenseCategory(
            id = "cat_" + UUID.randomUUID().toString().take(8),
            name = name,
            iconName = iconName,
            colorValue = colorValue,
            isDefault = false
        )
        scope.launch {
            dbHelper.insertCategory(cat)
            _categories.value = dbHelper.getAllCategories()
        }
    }

    fun updateCategory(category: ExpenseCategory) {
        scope.launch {
            dbHelper.updateCategory(category)
            _categories.value = dbHelper.getAllCategories()
        }
    }

    fun deleteCategory(id: String) {
        scope.launch {
            dbHelper.deleteCategory(id)
            _categories.value = dbHelper.getAllCategories()
        }
    }

    fun addTrip(
        name: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        budget: Double?,
        currency: String = "EUR"
    ) {
        val trip = Trip(
            id = "trip_" + UUID.randomUUID().toString().take(8),
            name = name,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            budget = budget,
            currency = currency,
            isDefault = false
        )
        scope.launch {
            dbHelper.insertTrip(trip)
            _trips.value = dbHelper.getAllTrips()
        }
    }

    fun updateTrip(trip: Trip) {
        scope.launch {
            dbHelper.updateTrip(trip)
            _trips.value = dbHelper.getAllTrips()
        }
    }

    fun deleteTrip(id: String) {
        scope.launch {
            dbHelper.deleteTrip(id)
            _trips.value = dbHelper.getAllTrips()
        }
    }

    fun clearAllExpenses() {
        scope.launch {
            dbHelper.clearAllData()
            _expenses.value = emptyList()
        }
    }

    fun seedDemoData() {
        scope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            fun daysAgo(days: Int, hour: Int = 12): Long {
                return (cal.clone() as Calendar).apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -days)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, 30)
                }.timeInMillis
            }

            val demoList = listOf(
                Expense(UUID.randomUUID().toString(), "Wocheneinkauf REWE", 68.40, "EUR", "cat_lebensmittel", null, daysAgo(0, 11), "Obst, Gemüse, Milch & Vorrat", "Karte", 48.1375, 11.5750, "München Marienplatz", feelingEmoji = null),
                Expense(UUID.randomUUID().toString(), "Mittagessen Trattoria", 14.50, "EUR", "cat_gastronomie", null, daysAgo(0, 13), "Pizza & Espresso", "Bar", 48.1360, 11.5770, "Trattoria Centro", feelingEmoji = "😊"),
                Expense(UUID.randomUUID().toString(), "Miete & Nebenkosten", 850.00, "EUR", "cat_wohnen", null, daysAgo(1, 8), "Monatsüberweisung", "Online", 48.1400, 11.5650, "Wohnung", feelingEmoji = null),
                Expense(UUID.randomUUID().toString(), "Tanken Aral Super E10", 56.80, "EUR", "cat_transport", null, daysAgo(2, 17), "42 Liter Benzin", "Karte", 48.1650, 11.5950, "Aral Tankstelle", feelingEmoji = "💸"),
                Expense(UUID.randomUUID().toString(), "Fitnessstudio Monatsbeitrag", 35.00, "EUR", "cat_abos", null, daysAgo(3, 9), "FitX Mitgliedschaft", "Online", 48.1300, 11.5900, "Gym Studio", feelingEmoji = "😊"),
                Expense(UUID.randomUUID().toString(), "Netflix & Spotify Abos", 22.98, "EUR", "cat_abos", null, daysAgo(4, 10), "Streaming Dienste", "Online", 48.1400, 11.5700, "Online", feelingEmoji = "🥱"),
                Expense(UUID.randomUUID().toString(), "Neue Sneaker H&M", 79.95, "EUR", "cat_shopping", null, daysAgo(5, 15), "Schuhe & Shirt", "Karte", 48.1380, 11.5730, "Kaufingerstraße", feelingEmoji = "⚡"),
                Expense(UUID.randomUUID().toString(), "Kino & Popcorn am Abend", 24.50, "EUR", "cat_freizeit", null, daysAgo(7, 20), "Cinema Preview", "Karte", 48.1450, 11.5620, "Filmpalast", feelingEmoji = "🥳"),
                Expense(UUID.randomUUID().toString(), "Apotheke Schmerzmittel", 12.80, "EUR", "cat_gesundheit", null, daysAgo(10, 11), "Ibuprofen & Pflaster", "Bar", 48.1360, 11.5760, "Apotheke am Markt", feelingEmoji = "🤔"),
                Expense(UUID.randomUUID().toString(), "Wochenmarkt frisches Obst", 15.20, "EUR", "cat_lebensmittel", null, daysAgo(12, 10), "Äpfel, Beeren & Nüsse", "Bar", 48.1351, 11.5761, "Viktualienmarkt", feelingEmoji = null),
                Expense(UUID.randomUUID().toString(), "Stromabschlag Stadtwerke", 65.00, "EUR", "cat_wohnen", null, daysAgo(15, 8), "Monatlicher Abschlag", "Online", 48.1370, 11.5780, "Stadtwerke München", feelingEmoji = null),
                Expense(UUID.randomUUID().toString(), "Kaffee & Croissant Bäckerei", 4.80, "EUR", "cat_gastronomie", null, daysAgo(18, 9), "Frühstück to go", "Bar", 48.1370, 11.5750, "Bäckerei Müller", feelingEmoji = "☕"),
                Expense(UUID.randomUUID().toString(), "Online Frust-Bestellung Deko", 42.00, "EUR", "cat_shopping", null, daysAgo(6, 22), "Spontankauf nachts", "Online", 48.1390, 11.5740, "Online Store", feelingEmoji = "🤦")
            )

            demoList.forEach { dbHelper.insertExpense(it) }
            _expenses.value = dbHelper.getAllExpenses()
        }
    }
}
