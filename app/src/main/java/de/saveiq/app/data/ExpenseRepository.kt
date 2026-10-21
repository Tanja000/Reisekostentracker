package de.saveiq.app.data

import android.content.Context
import de.saveiq.app.model.Expense
import de.saveiq.app.model.ExpenseCategory
import de.saveiq.app.model.RecurringExpense
import de.saveiq.app.model.RecurringInterval
import de.saveiq.app.model.Trip
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

    private val _recurringExpenses = MutableStateFlow<List<RecurringExpense>>(emptyList())
    val recurringExpenses: StateFlow<List<RecurringExpense>> = _recurringExpenses.asStateFlow()

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
            Expense(UUID.randomUUID().toString(), "Wocheneinkauf REWE", 68.40, "EUR", "cat_lebensmittel", null, daysAgo(1, 11), "Obst, Gemüse, Milch & Vorrat", "Karte", 48.1375, 11.5750, "München Marienplatz", feelingEmoji = null),
            Expense(UUID.randomUUID().toString(), "Mittagessen Trattoria", 14.50, "EUR", "cat_gastronomie", null, daysAgo(1, 13), "Pizza & Espresso", "Bar", 48.1360, 11.5770, "Trattoria Centro", feelingEmoji = "😊"),
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

    private fun advanceDueDate(currentDue: Long, interval: RecurringInterval): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDue }
        when (interval) {
            RecurringInterval.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurringInterval.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringInterval.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun insertInitialRecurringData() {
        val now = System.currentTimeMillis()
        val nextMonthCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }
        val nextMonth = nextMonthCal.timeInMillis

        val defaultRecs = listOf(
            RecurringExpense(
                id = "rec_rent",
                title = "Miete & Nebenkosten",
                amount = 850.00,
                currency = "EUR",
                categoryId = "cat_wohnen",
                interval = RecurringInterval.MONTHLY,
                startDate = now,
                lastExecutionDate = now,
                nextDueDate = nextMonth,
                paymentMethod = "Dauerauftrag",
                latitude = 48.1400,
                longitude = 11.5650,
                locationName = "Wohnung",
                notes = "Monatliche Warmmiete"
            ),
            RecurringExpense(
                id = "rec_gym",
                title = "Fitnessstudio FitX",
                amount = 35.00,
                currency = "EUR",
                categoryId = "cat_abos",
                interval = RecurringInterval.MONTHLY,
                startDate = now,
                lastExecutionDate = now,
                nextDueDate = nextMonth,
                paymentMethod = "Online",
                latitude = 48.1300,
                longitude = 11.5900,
                locationName = "Gym Studio",
                notes = "Monatsbeitrag"
            ),
            RecurringExpense(
                id = "rec_streaming",
                title = "Netflix & Spotify Abos",
                amount = 22.98,
                currency = "EUR",
                categoryId = "cat_abos",
                interval = RecurringInterval.MONTHLY,
                startDate = now,
                lastExecutionDate = now,
                nextDueDate = nextMonth,
                paymentMethod = "Online",
                latitude = 48.1400,
                longitude = 11.5700,
                locationName = "Online",
                notes = "Streaming Bundle"
            )
        )
        defaultRecs.forEach { dbHelper.insertRecurringExpense(it) }
    }

    fun processRecurringExpenses() {
        val now = System.currentTimeMillis()
        val allRecs = dbHelper.getAllRecurringExpenses()
        var updatedAny = false

        for (rec in allRecs) {
            if (!rec.isActive) continue
            var due = if (rec.nextDueDate > 0L) rec.nextDueDate else rec.startDate
            var bookedCycles = 0
            val maxCycles = 24 // Safety limit

            while (due <= now && bookedCycles < maxCycles) {
                val newExp = Expense(
                    id = UUID.randomUUID().toString(),
                    title = rec.title,
                    amount = rec.amount,
                    currency = rec.currency,
                    categoryId = rec.categoryId,
                    tripId = null,
                    date = due,
                    notes = rec.notes.trim(),
                    paymentMethod = rec.paymentMethod,
                    latitude = rec.latitude,
                    longitude = rec.longitude,
                    locationName = rec.locationName,
                    feelingEmoji = null
                )
                dbHelper.insertExpense(newExp)
                due = advanceDueDate(due, rec.interval)
                bookedCycles++
                updatedAny = true
            }

            if (bookedCycles > 0) {
                dbHelper.updateRecurringExpense(
                    rec.copy(
                        lastExecutionDate = now,
                        nextDueDate = due
                    )
                )
            }
        }

        _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
        if (updatedAny) {
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    private fun refreshAll() {
        scope.launch {
            _categories.value = dbHelper.getAllCategories()
            _trips.value = dbHelper.getAllTrips()

            val hasSeeded = prefs.getBoolean("has_seeded_initial_v5", false)
            if (!hasSeeded) {
                if (dbHelper.getAllExpenses().isEmpty()) {
                    insertInitialDemoData()
                }
                if (dbHelper.getAllRecurringExpenses().isEmpty()) {
                    insertInitialRecurringData()
                }
                prefs.edit().putBoolean("has_seeded_initial_v5", true).apply()
            }

            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()

            // Process any due recurring expenses automatically
            processRecurringExpenses()

            _expenses.value = dbHelper.getAllExpenses()
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
            val all = dbHelper.getAllExpenses()
            _expenses.value = listOf(expense) + all.filter { it.id != expense.id }
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
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun updateCategory(category: ExpenseCategory) {
        scope.launch {
            dbHelper.updateCategory(category)
            _categories.value = dbHelper.getAllCategories()
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun deleteCategory(id: String) {
        scope.launch {
            dbHelper.deleteCategory(id)
            _categories.value = dbHelper.getAllCategories()
            _expenses.value = dbHelper.getAllExpenses()
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

    // Recurring Expenses Operations
    fun addRecurringExpense(rec: RecurringExpense, bookInitialExpense: Boolean = true) {
        scope.launch {
            dbHelper.insertRecurringExpense(rec)
            if (bookInitialExpense) {
                // Immediately book current cycle so it appears across ALL tabs:
                val now = System.currentTimeMillis()
                val initialExp = Expense(
                    id = UUID.randomUUID().toString(),
                    title = rec.title,
                    amount = rec.amount,
                    currency = rec.currency,
                    categoryId = rec.categoryId,
                    tripId = null,
                    date = if (rec.startDate > 0L) rec.startDate else now,
                    notes = rec.notes.trim(),
                    paymentMethod = rec.paymentMethod,
                    latitude = rec.latitude,
                    longitude = rec.longitude,
                    locationName = rec.locationName,
                    feelingEmoji = null
                )
                dbHelper.insertExpense(initialExp)
            }
            processRecurringExpenses()
            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
            _expenses.value = dbHelper.getAllExpenses()
        }
    }

    fun addRecurringExpenseWithInitialBooking(rec: RecurringExpense, initialExpense: Expense) {
        scope.launch {
            dbHelper.insertRecurringExpense(rec)
            dbHelper.insertExpense(initialExpense)
            processRecurringExpenses()
            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
            val all = dbHelper.getAllExpenses()
            _expenses.value = listOf(initialExpense) + all.filter { it.id != initialExpense.id }
        }
    }

    fun updateRecurringExpense(rec: RecurringExpense) {
        scope.launch {
            dbHelper.updateRecurringExpense(rec)
            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
        }
    }

    fun deleteRecurringExpense(id: String) {
        scope.launch {
            dbHelper.deleteRecurringExpense(id)
            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
        }
    }

    fun triggerRecurringNow(id: String) {
        scope.launch {
            val rec = dbHelper.getAllRecurringExpenses().find { it.id == id } ?: return@launch
            val now = System.currentTimeMillis()
            val newExp = Expense(
                id = UUID.randomUUID().toString(),
                title = rec.title,
                amount = rec.amount,
                currency = rec.currency,
                categoryId = rec.categoryId,
                tripId = null,
                date = now,
                notes = rec.notes.trim(),
                paymentMethod = rec.paymentMethod,
                latitude = rec.latitude,
                longitude = rec.longitude,
                locationName = rec.locationName,
                feelingEmoji = null
            )
            dbHelper.insertExpense(newExp)
            val nextDue = advanceDueDate(if (rec.nextDueDate > now) rec.nextDueDate else now, rec.interval)
            dbHelper.updateRecurringExpense(
                rec.copy(
                    lastExecutionDate = now,
                    nextDueDate = nextDue
                )
            )
            _recurringExpenses.value = dbHelper.getAllRecurringExpenses()
            val all = dbHelper.getAllExpenses()
            _expenses.value = listOf(newExp) + all.filter { it.id != newExp.id }
        }
    }
}
