package de.saveiq.app.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.saveiq.app.data.AiFrustrationReport
import de.saveiq.app.data.AiOpportunityInsight
import de.saveiq.app.data.AndroidAiCoreClient
import de.saveiq.app.data.ExpenseRepository
import de.saveiq.app.data.GeoPlace
import de.saveiq.app.data.GeoPlaceLibrary
import de.saveiq.app.data.WorldBankApiService
import de.saveiq.app.model.AppLanguage
import de.saveiq.app.model.Expense
import de.saveiq.app.model.ExpenseCategory
import de.saveiq.app.model.ExpenseStatistics
import de.saveiq.app.model.RecurringExpense
import de.saveiq.app.model.RecurringInterval
import de.saveiq.app.model.Trip
import de.saveiq.app.ui.components.ChartMetricScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class TimeFilter(val labelDe: String, val labelEn: String) {
    ALL("Gesamt", "All"),
    THIS_YEAR("Dieses Jahr", "This Year"),
    THIS_MONTH("Dieser Monat", "This Month"),
    THIS_WEEK("Diese Woche", "This Week"),
    TODAY("Heute", "Today");

    val label: String get() = labelDe
    fun getLabel(lang: AppLanguage): String = if (lang.isEnglish) labelEn else labelDe
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ExpenseRepository(application.applicationContext)
    private val prefs = application.getSharedPreferences("rk_prefs", Context.MODE_PRIVATE)

    val expenses: StateFlow<List<Expense>> = repository.expenses
    val recurringExpenses: StateFlow<List<RecurringExpense>> = repository.recurringExpenses
    val categories: StateFlow<List<ExpenseCategory>> = repository.categories
    val trips: StateFlow<List<Trip>> = repository.trips

    // Language state (persisted across restarts, supports DE and EN with British flag toggle)
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", "de"))
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        val next = _currentLanguage.value.toggle()
        _currentLanguage.value = next
        prefs.edit().putString("app_language", next.code).apply()
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    // World Bank API & Android AICore Client Services
    val worldBankService = WorldBankApiService()
    val aiCoreClient = AndroidAiCoreClient(application.applicationContext)

    private val _worldBankInflationRate = MutableStateFlow(2.25)
    val worldBankInflationRate: StateFlow<Double> = _worldBankInflationRate.asStateFlow()

    private val _aiFrustrationReport = MutableStateFlow<AiFrustrationReport?>(null)
    val aiFrustrationReport: StateFlow<AiFrustrationReport?> = _aiFrustrationReport.asStateFlow()

    private val _aiOpportunityInsights = MutableStateFlow<List<AiOpportunityInsight>>(emptyList())
    val aiOpportunityInsights: StateFlow<List<AiOpportunityInsight>> = _aiOpportunityInsights.asStateFlow()

    // Dark Mode state
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Monthly Budget state (persisted across restarts)
    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 1500f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    // Location permission consent state
    private val _hasLocationConsent = MutableStateFlow(prefs.getBoolean("gps_consent", false))
    val hasLocationConsent: StateFlow<Boolean> = _hasLocationConsent.asStateFlow()

    private val _isLocationConsentDialogOpen = MutableStateFlow(!prefs.getBoolean("gps_consent_asked", false))
    val isLocationConsentDialogOpen: StateFlow<Boolean> = _isLocationConsentDialogOpen.asStateFlow()

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation.asStateFlow()

    private val _lastLocationName = MutableStateFlow<String?>("GPS wird ermittelt...")
    val lastLocationName: StateFlow<String?> = _lastLocationName.asStateFlow()

    init {
        if (_hasLocationConsent.value) {
            fetchCurrentLocation()
        }
        refreshInflationRate(forceRefresh = true)
        viewModelScope.launch {
            expenses.collect { list ->
                refreshAiInsights(list, _worldBankInflationRate.value)
            }
        }
    }

    fun refreshInflationRate(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            val rate = worldBankService.getGermanyInflationRate(forceRefresh)
            _worldBankInflationRate.value = rate
            refreshAiInsights(expenses.value, rate)
        }
    }

    fun refreshAiInsights(
        list: List<Expense> = expenses.value,
        rate: Double = _worldBankInflationRate.value
    ) {
        viewModelScope.launch {
            val report = aiCoreClient.analyzeFrustrationPurchases(list)
            _aiFrustrationReport.value = report
            val opps = aiCoreClient.generateCreativeOpportunitySentences(list, rate, worldBankService)
            _aiOpportunityInsights.value = opps
        }
    }

    fun openLocationConsentDialog() {
        _isLocationConsentDialogOpen.value = true
    }

    fun setLocationConsent(granted: Boolean) {
        _hasLocationConsent.value = granted
        _isLocationConsentDialogOpen.value = false
        prefs.edit()
            .putBoolean("gps_consent", granted)
            .putBoolean("gps_consent_asked", true)
            .apply()
        if (granted) {
            fetchCurrentLocation()
        }
    }

    fun fetchCurrentLocation() {
        val app = getApplication<Application>()
        val fineGranted = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            _lastLocationName.value = "GPS-Berechtigung fehlt"
            return
        }

        try {
            val lm = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            val providers = lm.getProviders(true)

            // Check best last known location
            var bestLoc: Location? = null
            for (p in providers) {
                val loc = try { lm.getLastKnownLocation(p) } catch (_: SecurityException) { null }
                if (loc != null && (bestLoc == null || loc.time > bestLoc.time)) {
                    bestLoc = loc
                }
            }
            if (bestLoc != null) {
                _lastKnownLocation.value = bestLoc
                reverseGeocode(bestLoc)
            }

            // Fresh location on Android 30+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cancellationSignal = CancellationSignal()
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try {
                        lm.getCurrentLocation(
                            LocationManager.GPS_PROVIDER,
                            cancellationSignal,
                            ContextCompat.getMainExecutor(app)
                        ) { loc ->
                            if (loc != null) {
                                _lastKnownLocation.value = loc
                                reverseGeocode(loc)
                            }
                        }
                    } catch (_: SecurityException) {}
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        lm.getCurrentLocation(
                            LocationManager.NETWORK_PROVIDER,
                            cancellationSignal,
                            ContextCompat.getMainExecutor(app)
                        ) { loc ->
                            if (loc != null && (_lastKnownLocation.value == null || loc.accuracy < (_lastKnownLocation.value?.accuracy ?: 999f))) {
                                _lastKnownLocation.value = loc
                                reverseGeocode(loc)
                            }
                        }
                    } catch (_: SecurityException) {}
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        _lastKnownLocation.value = loc
                        reverseGeocode(loc)
                        try { lm.removeUpdates(this) } catch (_: Exception) {}
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                for (p in providers) {
                    try {
                        lm.requestLocationUpdates(p, 1000L, 5f, listener, app.mainLooper)
                    } catch (_: SecurityException) {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun reverseGeocode(loc: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(app, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                        val street = addr.thoroughfare
                        val name = when {
                            street != null && city != null -> "$street, $city"
                            city != null -> city
                            else -> "${String.format(Locale.US, "%.3f", loc.latitude)}, ${String.format(Locale.US, "%.3f", loc.longitude)}"
                        }
                        _lastLocationName.value = name
                        return@launch
                    }
                }
            } catch (_: Exception) {}
            _lastLocationName.value = "GPS (${String.format(Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)})"
        }
    }

    fun quickAddExpense(
        title: String,
        amount: Double,
        categoryId: String,
        paymentMethod: String,
        customLocation: String? = null,
        feelingEmoji: String? = null
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val resolvedPlace = if (!customLocation.isNullOrBlank()) {
                GeoPlaceLibrary.resolveLocation(customLocation, app)
            } else {
                val loc = _lastKnownLocation.value
                val locName = _lastLocationName.value
                if (loc != null) {
                    GeoPlace(locName ?: "Aktueller Standort", loc.latitude, loc.longitude)
                } else if (!locName.isNullOrBlank() && !locName.contains("fehlt") && !locName.contains("ermittelt")) {
                    GeoPlaceLibrary.resolveLocation(locName, app)
                } else {
                    GeoPlaceLibrary.resolveLocation("Karlsruhe", app)
                }
            }

            val finalEmoji = if (de.saveiq.app.model.PurchaseEmotions.shouldAskEmotion(categoryId)) feelingEmoji else null

            repository.addExpense(
                title = title,
                amount = amount,
                currency = "EUR",
                categoryId = categoryId,
                tripId = null,
                date = System.currentTimeMillis(),
                notes = "",
                paymentMethod = paymentMethod,
                latitude = resolvedPlace.latitude,
                longitude = resolvedPlace.longitude,
                locationName = resolvedPlace.name,
                feelingEmoji = finalEmoji
            )
        }
    }

    // Chart Scope
    private val _chartScope = MutableStateFlow(ChartMetricScope.TOTAL)
    val chartScope: StateFlow<ChartMetricScope> = _chartScope.asStateFlow()

    // Filter states
    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId: StateFlow<String?> = _selectedTripId.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    // Dialog states
    var isExpenseDialogOpen = MutableStateFlow(false)
        private set
    var editingExpense = MutableStateFlow<Expense?>(null)
        private set

    var isCategoryDialogOpen = MutableStateFlow(false)
        private set
    var editingCategory = MutableStateFlow<ExpenseCategory?>(null)
        private set

    var isTripDialogOpen = MutableStateFlow(false)
        private set
    var editingTrip = MutableStateFlow<Trip?>(null)
        private set

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        expenses,
        timeFilter,
        searchQuery,
        selectedCategoryId
    ) { allExpenses, filter, query, catId ->
        allExpenses.filter { exp ->
            val matchCategory = (catId == null) || (exp.categoryId == catId)
            val matchQuery = query.isBlank() ||
                    exp.title.contains(query, ignoreCase = true) ||
                    exp.notes.contains(query, ignoreCase = true)

            val matchTime = when (filter) {
                TimeFilter.ALL -> true
                TimeFilter.TODAY -> isSameDay(exp.date, System.currentTimeMillis())
                TimeFilter.THIS_WEEK -> isSameWeek(exp.date, System.currentTimeMillis())
                TimeFilter.THIS_MONTH -> isSameMonth(exp.date, System.currentTimeMillis())
                TimeFilter.THIS_YEAR -> isSameYear(exp.date, System.currentTimeMillis())
            }

            matchCategory && matchQuery && matchTime
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val statistics: StateFlow<ExpenseStatistics> = combine(
        filteredExpenses,
        categories
    ) { curExpenses, curCategories ->
        ExpenseStatistics.calculate(curExpenses, curCategories)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpenseStatistics.empty())

    fun toggleDarkTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        prefs.edit().putBoolean("dark_mode", newVal).apply()
    }

    fun setChartScope(scope: ChartMetricScope) {
        _chartScope.value = scope
    }

    fun setTripFilter(tripId: String?) {
        _selectedTripId.value = tripId
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun openAddExpenseDialog() {
        editingExpense.value = null
        isExpenseDialogOpen.value = true
    }

    fun openEditExpenseDialog(expense: Expense) {
        editingExpense.value = expense
        isExpenseDialogOpen.value = true
    }

    fun closeExpenseDialog() {
        isExpenseDialogOpen.value = false
        editingExpense.value = null
    }

    fun openAddCategoryDialog() {
        editingCategory.value = null
        isCategoryDialogOpen.value = true
    }

    fun openEditCategoryDialog(cat: ExpenseCategory) {
        editingCategory.value = cat
        isCategoryDialogOpen.value = true
    }

    fun closeCategoryDialog() {
        isCategoryDialogOpen.value = false
        editingCategory.value = null
    }

    fun openAddTripDialog() {
        editingTrip.value = null
        isTripDialogOpen.value = true
    }

    fun openEditTripDialog(trip: Trip) {
        editingTrip.value = trip
        isTripDialogOpen.value = true
    }

    fun closeTripDialog() {
        isTripDialogOpen.value = false
        editingTrip.value = null
    }

    fun saveExpense(
        title: String,
        amount: Double,
        currency: String,
        categoryId: String,
        tripId: String?,
        date: Long,
        notes: String,
        paymentMethod: String,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null,
        feelingEmoji: String? = null,
        isRecurring: Boolean = false,
        recurringInterval: RecurringInterval = RecurringInterval.MONTHLY
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val current = editingExpense.value
            val enteredName = locationName?.trim()?.ifEmpty { null }
            val rawName = enteredName ?: current?.locationName ?: _lastLocationName.value ?: "Karlsruhe"

            val resolved = if (latitude != null && longitude != null) {
                GeoPlace(rawName, latitude, longitude)
            } else {
                GeoPlaceLibrary.resolveLocation(rawName, app)
            }

            val finalEmoji = if (de.saveiq.app.model.PurchaseEmotions.shouldAskEmotion(categoryId)) feelingEmoji else null

            if (current != null) {
                repository.updateExpense(
                    current.copy(
                        title = title,
                        amount = amount,
                        currency = currency,
                        categoryId = categoryId,
                        tripId = tripId,
                        date = date,
                        notes = notes,
                        paymentMethod = paymentMethod,
                        latitude = resolved.latitude,
                        longitude = resolved.longitude,
                        locationName = resolved.name,
                        feelingEmoji = finalEmoji
                    )
                )
            } else {
                repository.addExpense(
                    title = title,
                    amount = amount,
                    currency = currency,
                    categoryId = categoryId,
                    tripId = tripId,
                    date = date,
                    notes = notes,
                    paymentMethod = paymentMethod,
                    latitude = resolved.latitude,
                    longitude = resolved.longitude,
                    locationName = resolved.name,
                    feelingEmoji = finalEmoji
                )

                // If marked as recurring, also register a RecurringExpense rule
                if (isRecurring) {
                    val cal = Calendar.getInstance().apply { timeInMillis = date }
                    when (recurringInterval) {
                        RecurringInterval.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                        RecurringInterval.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                        RecurringInterval.MONTHLY -> cal.add(Calendar.MONTH, 1)
                        RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
                    }
                    val nextDue = cal.timeInMillis

                    repository.addRecurringExpense(
                        RecurringExpense(
                            id = "rec_" + java.util.UUID.randomUUID().toString().take(8),
                            title = title,
                            amount = amount,
                            currency = currency,
                            categoryId = categoryId,
                            interval = recurringInterval,
                            startDate = date,
                            lastExecutionDate = date,
                            nextDueDate = nextDue,
                            paymentMethod = paymentMethod,
                            latitude = resolved.latitude,
                            longitude = resolved.longitude,
                            locationName = resolved.name,
                            notes = notes.trim(),
                            isActive = true
                        ),
                        bookInitialExpense = false
                    )
                }
            }
            closeExpenseDialog()
        }
    }

    fun addRecurringExpense(
        title: String,
        amount: Double,
        categoryId: String,
        interval: RecurringInterval,
        paymentMethod: String = "Dauerauftrag",
        locationName: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val enteredName = locationName?.trim()?.ifEmpty { null }
            val rawName = enteredName ?: _lastLocationName.value ?: "Karlsruhe"
            val resolved = GeoPlaceLibrary.resolveLocation(rawName, app)

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            when (interval) {
                RecurringInterval.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurringInterval.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurringInterval.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
            }
            val rec = RecurringExpense(
                id = "rec_" + java.util.UUID.randomUUID().toString().take(8),
                title = title,
                amount = amount,
                currency = "EUR",
                categoryId = categoryId,
                interval = interval,
                startDate = now,
                lastExecutionDate = now,
                nextDueDate = cal.timeInMillis,
                paymentMethod = paymentMethod,
                latitude = resolved.latitude,
                longitude = resolved.longitude,
                locationName = resolved.name,
                notes = notes,
                isActive = true
            )

            // Immediately create the active Expense booking so it appears in ALL tabs:
            // Ausgaben, Statistiken, Karte, Budgets, Spar-KI!
            val initialExpense = Expense(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                amount = amount,
                currency = "EUR",
                categoryId = categoryId,
                tripId = null,
                date = now,
                notes = notes.trim(),
                paymentMethod = paymentMethod,
                latitude = resolved.latitude,
                longitude = resolved.longitude,
                locationName = resolved.name,
                feelingEmoji = null
            )

            repository.addRecurringExpenseWithInitialBooking(rec, initialExpense)
        }
    }

    fun deleteRecurringExpense(id: String) {
        repository.deleteRecurringExpense(id)
    }

    fun triggerRecurringExpenseNow(id: String) {
        repository.triggerRecurringNow(id)
    }

    fun deleteExpense(id: String) {
        repository.deleteExpense(id)
    }

    fun saveCategory(name: String, iconName: String, colorValue: Long) {
        val current = editingCategory.value
        if (current != null) {
            repository.updateCategory(
                current.copy(
                    name = name,
                    iconName = iconName,
                    colorValue = colorValue
                )
            )
        } else {
            repository.addCategory(name, iconName, colorValue)
        }
        _selectedCategoryId.value = null
        closeCategoryDialog()
    }

    fun deleteCategory(id: String) {
        if (_selectedCategoryId.value == id) {
            _selectedCategoryId.value = null
        }
        repository.deleteCategory(id)
    }

    fun saveTrip(
        name: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        budget: Double?,
        currency: String
    ) {
        val current = editingTrip.value
        if (current != null) {
            repository.updateTrip(
                current.copy(
                    name = name,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    budget = budget,
                    currency = currency
                )
            )
        } else {
            repository.addTrip(name, destination, startDate, endDate, budget, currency)
        }
        closeTripDialog()
    }

    fun deleteTrip(id: String) {
        repository.deleteTrip(id)
    }

    fun seedDemoData() {
        repository.seedDemoData()
    }

    fun clearAllData() {
        repository.clearAllExpenses()
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
    }

    private fun isSameYear(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }
}
