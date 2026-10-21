package com.example.reisekostentracker.model

enum class AppLanguage(val code: String, val flag: String, val displayName: String) {
    DE("de", "🇩🇪", "Deutsch"),
    EN("en", "🇬🇧", "English");

    val isEnglish: Boolean get() = this == EN

    fun toggle(): AppLanguage = if (this == DE) EN else DE

    companion object {
        fun fromCode(code: String?): AppLanguage =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DE
    }
}

object AppStrings {
    // Nav Items
    fun navExpenses(lang: AppLanguage) = if (lang.isEnglish) "Expenses" else "Ausgaben"
    fun navStatistics(lang: AppLanguage) = if (lang.isEnglish) "Statistics" else "Statistik"
    fun navSavingsAi(lang: AppLanguage) = if (lang.isEnglish) "Save-AI" else "Spar-KI"
    fun navMap(lang: AppLanguage) = if (lang.isEnglish) "Map" else "Karte"
    fun navCategories(lang: AppLanguage) = if (lang.isEnglish) "Categories" else "Kategorien"
    fun navBudgets(lang: AppLanguage) = if (lang.isEnglish) "Budgets" else "Budgets"

    // Common
    fun totalExpenses(lang: AppLanguage) = if (lang.isEnglish) "Total Expenses" else "Gesamtausgaben"
    fun entriesCount(count: Int, lang: AppLanguage) =
        if (lang.isEnglish) "$count entries" else "$count Einträge"
    fun searchPlaceholder(lang: AppLanguage) =
        if (lang.isEnglish) "Search expenses..." else "Ausgabe durchsuchen..."
    fun quickAdd(lang: AppLanguage) = if (lang.isEnglish) "⚡ Quick Add" else "⚡ Schnellbuchung"
    fun quickAddHint(lang: AppLanguage) =
        if (lang.isEnglish) "Tap to adjust & book" else "Klick zum Anpassen & Buchen"
    fun addExpense(lang: AppLanguage) = if (lang.isEnglish) "Add expense" else "Ausgabe hinzufügen"
    fun editExpense(lang: AppLanguage) = if (lang.isEnglish) "Edit expense" else "Ausgabe bearbeiten"
    fun newExpense(lang: AppLanguage) = if (lang.isEnglish) "New Expense" else "Neue Ausgabe"
    fun noExpensesFound(lang: AppLanguage) =
        if (lang.isEnglish) "No expenses found" else "Keine Ausgaben gefunden"
    fun delete(lang: AppLanguage) = if (lang.isEnglish) "Delete" else "Löschen"
    fun save(lang: AppLanguage) = if (lang.isEnglish) "Save" else "Speichern"
    fun cancel(lang: AppLanguage) = if (lang.isEnglish) "Cancel" else "Abbrechen"
    fun understood(lang: AppLanguage) = if (lang.isEnglish) "Understood" else "Verstanden"
    fun location(lang: AppLanguage) = if (lang.isEnglish) "Location" else "Ort"
    fun paymentMethod(lang: AppLanguage) = if (lang.isEnglish) "Payment method" else "Zahlungsart"
    fun notes(lang: AppLanguage) = if (lang.isEnglish) "Notes" else "Notizen"
    fun title(lang: AppLanguage) = if (lang.isEnglish) "Title" else "Titel"
    fun amount(lang: AppLanguage) = if (lang.isEnglish) "Amount" else "Betrag"
    fun category(lang: AppLanguage) = if (lang.isEnglish) "Category" else "Kategorie"

    // Recurring Expenses
    fun recurringExpenses(lang: AppLanguage) =
        if (lang.isEnglish) "Recurring Expenses" else "Wiederkehrende Ausgaben"
    fun recurring(lang: AppLanguage) =
        if (lang.isEnglish) "Recurring" else "Wiederkehrend"
    fun autoRecord(lang: AppLanguage) =
        if (lang.isEnglish) "Automatically recorded" else "Automatisch erfasst"
    fun markAsRecurring(lang: AppLanguage) =
        if (lang.isEnglish) "Record as recurring expense (auto-booking)" else "Als wiederkehrende Ausgabe anlegen (automatisch buchen)"
    fun interval(lang: AppLanguage) =
        if (lang.isEnglish) "Interval" else "Intervall"
    fun nextBooking(lang: AppLanguage) =
        if (lang.isEnglish) "Next booking" else "Nächste Buchung"
    fun bookNow(lang: AppLanguage) =
        if (lang.isEnglish) "Book now" else "Jetzt buchen"
    fun addRecurringTitle(lang: AppLanguage) =
        if (lang.isEnglish) "+ Add Recurring Expense" else "+ Neuer Dauerauftrag"
    fun recurringSubtitle(lang: AppLanguage) =
        if (lang.isEnglish) "Active subscriptions & standing orders" else "Aktive Abonnements & Daueraufträge"
    fun autoBooked(interval: String, lang: AppLanguage) =
        if (lang.isEnglish) "Auto-booked ($interval)" else "Automatisch gebucht ($interval)"
    fun noRecurringExpenses(lang: AppLanguage) =
        if (lang.isEnglish) "No recurring expenses set up yet" else "Noch keine wiederkehrenden Ausgaben angelegt"

    // Spar-KI
    fun savingsHacks(lang: AppLanguage) =
        if (lang.isEnglish) "Savings Hacks" else "Spar-Hacks"
    fun compoundInterestTitle(lang: AppLanguage) =
        if (lang.isEnglish) "What-If Compound Interest" else "Was-Wäre-Wenn Zinseszins"
    fun worldBankInflation(rate: Double, lang: AppLanguage) =
        if (lang.isEnglish) "World Bank Inflation ${String.format(java.util.Locale.US, "%.1f", rate)}%"
        else "Weltbank-Inflation ${String.format(java.util.Locale.GERMAN, "%.1f", rate)}%"

    // Categories Localizer
    fun categoryName(id: String, defaultName: String, lang: AppLanguage): String {
        if (!lang.isEnglish) return defaultName
        return when (id) {
            "cat_lebensmittel" -> "Groceries"
            "cat_gastronomie" -> "Dining & Cafés"
            "cat_wohnen" -> "Housing & Rent"
            "cat_transport" -> "Transport & Fuel"
            "cat_shopping" -> "Shopping & Clothing"
            "cat_freizeit" -> "Leisure & Fun"
            "cat_gesundheit" -> "Health & Pharmacy"
            "cat_abos" -> "Subscriptions & Gym"
            "cat_sonstiges" -> "Miscellaneous"
            else -> defaultName
        }
    }
}
