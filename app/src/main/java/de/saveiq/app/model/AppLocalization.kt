package de.saveiq.app.model

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
    // Navigation items
    fun navExpenses(lang: AppLanguage) = if (lang.isEnglish) "Expenses" else "Ausgaben"
    fun navStatistics(lang: AppLanguage) = if (lang.isEnglish) "Statistics" else "Statistik"
    fun navSavingsAi(lang: AppLanguage) = if (lang.isEnglish) "Save-AI" else "Spar-KI"
    fun navMap(lang: AppLanguage) = if (lang.isEnglish) "Map" else "Karte"
    fun navCategories(lang: AppLanguage) = if (lang.isEnglish) "Categories" else "Kategorien"
    fun navBudgets(lang: AppLanguage) = if (lang.isEnglish) "Budgets" else "Budgets"

    // Top Bar & Menu
    fun appTitle(lang: AppLanguage) = "SaveIQ"
    fun menuLoadDemo(lang: AppLanguage) =
        if (lang.isEnglish) "Load Demo Financial Data" else "Demo-Finanzdaten laden"
    fun menuDeleteAll(lang: AppLanguage) =
        if (lang.isEnglish) "Delete All Expenses" else "Alle Ausgaben löschen"

    // Delete All Confirmation Dialog
    fun deleteAllTitle(lang: AppLanguage) =
        if (lang.isEnglish) "Delete All Expenses?" else "Alle Ausgaben löschen?"
    fun deleteAllMessage(lang: AppLanguage) =
        if (lang.isEnglish)
            "Do you really want to permanently delete all recorded expenses? This action cannot be undone."
        else
            "Möchtest du wirklich alle eingetragenen Ausgaben unwiderruflich löschen? Diese Aktion kann nicht rückgängig gemacht werden."
    fun deleteAllConfirm(lang: AppLanguage) =
        if (lang.isEnglish) "Delete All" else "Alles löschen"

    // Common Buttons & Labels
    fun save(lang: AppLanguage) = if (lang.isEnglish) "Save" else "Speichern"
    fun add(lang: AppLanguage) = if (lang.isEnglish) "Add" else "Hinzufügen"
    fun cancel(lang: AppLanguage) = if (lang.isEnglish) "Cancel" else "Abbrechen"
    fun delete(lang: AppLanguage) = if (lang.isEnglish) "Delete" else "Löschen"
    fun edit(lang: AppLanguage) = if (lang.isEnglish) "Edit" else "Bearbeiten"
    fun understood(lang: AppLanguage) = if (lang.isEnglish) "Understood" else "Verstanden"
    fun close(lang: AppLanguage) = if (lang.isEnglish) "Close" else "Schließen"
    fun title(lang: AppLanguage) = if (lang.isEnglish) "Title" else "Titel"
    fun amount(lang: AppLanguage) = if (lang.isEnglish) "Amount" else "Betrag"
    fun amountInEuro(lang: AppLanguage) = if (lang.isEnglish) "Amount in €" else "Betrag in €"
    fun category(lang: AppLanguage) = if (lang.isEnglish) "Category" else "Kategorie"
    fun allCategories(lang: AppLanguage) = if (lang.isEnglish) "All Categories" else "Alle Kategorien"
    fun location(lang: AppLanguage) = if (lang.isEnglish) "Location" else "Ort"
    fun locationOptional(lang: AppLanguage) = if (lang.isEnglish) "Location (optional)" else "Ort / GPS-Standort (optional)"
    fun notes(lang: AppLanguage) = if (lang.isEnglish) "Notes" else "Notizen"
    fun notesOptional(lang: AppLanguage) = if (lang.isEnglish) "Notes (optional)" else "Notiz (optional)"
    fun paymentMethod(lang: AppLanguage) = if (lang.isEnglish) "Payment method" else "Zahlungsart"
    fun date(lang: AppLanguage) = if (lang.isEnglish) "Date" else "Datum"

    // Expenses Screen
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
    fun noExpensesHint(lang: AppLanguage) =
        if (lang.isEnglish) "Tap the '+' button to log a purchase, or load sample data."
        else "Tippe auf das '+' um Kosten hinzuzufügen, oder lade Beispieldaten."
    fun loadSampleData(lang: AppLanguage) =
        if (lang.isEnglish) "Load Sample Data" else "Beispieldaten laden"

    // Payment Methods
    fun paymentMethodName(method: String, lang: AppLanguage): String {
        if (!lang.isEnglish) return method
        return when (method.lowercase()) {
            "karte", "card" -> "Card"
            "bar", "cash" -> "Cash"
            "online", "paypal" -> "Online"
            "dauerauftrag", "standing order" -> "Standing order"
            else -> method
        }
    }

    // Recurring Expenses
    fun recurringExpenses(lang: AppLanguage) =
        if (lang.isEnglish) "Recurring Expenses" else "Wiederkehrende Ausgaben"
    fun recurring(lang: AppLanguage) =
        if (lang.isEnglish) "Recurring" else "Wiederkehrend"
    fun autoRecord(lang: AppLanguage) =
        if (lang.isEnglish) "Automatically recorded" else "Automatisch erfasst"
    fun markAsRecurring(lang: AppLanguage) =
        if (lang.isEnglish) "Record as recurring expense (auto-booking)"
        else "Als wiederkehrende Ausgabe anlegen (automatisch buchen)"
    fun recurringIntervalSub(lang: AppLanguage) =
        if (lang.isEnglish) "Automatically recorded every cycle"
        else "Automatisch in Intervallen erfassen"
    fun interval(lang: AppLanguage) =
        if (lang.isEnglish) "Interval" else "Intervall"
    fun nextBooking(lang: AppLanguage) =
        if (lang.isEnglish) "Next booking" else "Nächste Buchung"
    fun bookNow(lang: AppLanguage) =
        if (lang.isEnglish) "Book now" else "Jetzt buchen"
    fun addRecurringTitle(lang: AppLanguage) =
        if (lang.isEnglish) "+ Add Recurring Expense" else "+ Neuer Dauerauftrag"
    fun recurringSubtitle(lang: AppLanguage) =
        if (lang.isEnglish) "Active monthly, yearly & regular subscriptions"
        else "Aktive monatliche, jährliche & regelmäßige Fixkosten"
    fun autoBooked(interval: String, lang: AppLanguage) =
        if (lang.isEnglish) "Auto-booked ($interval)" else "Automatisch gebucht ($interval)"
    fun noRecurringExpenses(lang: AppLanguage) =
        if (lang.isEnglish) "No recurring expenses set up yet" else "Noch keine wiederkehrenden Ausgaben angelegt"

    // Interval Labels
    fun intervalLabel(interval: RecurringInterval, lang: AppLanguage): String {
        return if (lang.isEnglish) interval.labelEn else interval.labelDe
    }

    // Categories Localizer
    fun categoryName(id: String, defaultName: String, lang: AppLanguage): String {
        if (!lang.isEnglish) return defaultName
        return when (id.lowercase()) {
            "cat_lebensmittel" -> "Groceries"
            "cat_gastronomie" -> "Dining & Cafés"
            "cat_wohnen" -> "Housing & Rent"
            "cat_transport" -> "Transport & Fuel"
            "cat_shopping" -> "Shopping & Clothes"
            "cat_freizeit" -> "Leisure & Fun"
            "cat_gesundheit" -> "Health & Pharmacy"
            "cat_abos" -> "Subscriptions & Gym"
            "cat_sonstiges" -> "Miscellaneous"
            else -> defaultName
        }
    }

    // Spar-KI Screen
    fun sparKiTitle(lang: AppLanguage) =
        if (lang.isEnglish) "Save-AI & Money Habits" else "Spar-KI & Sparfuchs"
    fun sparKiSubtitle(lang: AppLanguage) =
        if (lang.isEnglish) "Smart impulse buy prevention & wealth impact"
        else "Intelligente Frustkauf-Vermeidung & Vermögens-Effekt"
    fun compoundInterestTitle(lang: AppLanguage) =
        if (lang.isEnglish) "What-If Compound Interest" else "Was-Wäre-Wenn Zinseszins"
    fun worldBankInflation(rate: Double, lang: AppLanguage) =
        if (lang.isEnglish) "World Bank Inflation UK ${String.format(java.util.Locale.US, "%.2f", rate)}%"
        else "Weltbank-Inflation DE ${String.format(java.util.Locale.GERMAN, "%.2f", rate)}%"
    fun savingsHacks(lang: AppLanguage) =
        if (lang.isEnglish) "Savings Hacks" else "Spar-Hacks"
    fun consciousSpendingScore(lang: AppLanguage) =
        if (lang.isEnglish) "Conscious Spending Score" else "Bewusstes Konsum-Level"
    fun avoidedFrustration(lang: AppLanguage) =
        if (lang.isEnglish) "Impulse & Frustration Spend" else "Frust- & Impulsausgaben"

    // Quick Add Presets
    fun quickCoffee(lang: AppLanguage) = if (lang.isEnglish) "Coffee & Snack" else "Kaffee & Snack"
    fun quickLunch(lang: AppLanguage) = if (lang.isEnglish) "Lunch" else "Mittagessen"
    fun quickSupermarket(lang: AppLanguage) = if (lang.isEnglish) "Groceries" else "Supermarkt"
    fun quickTransport(lang: AppLanguage) = if (lang.isEnglish) "Transit / Fuel" else "Tanken / Ticket"

    // Budgets Screen
    fun monthlyBudget(lang: AppLanguage) = if (lang.isEnglish) "Monthly Budget" else "Monatsbudget"
    fun spent(lang: AppLanguage) = if (lang.isEnglish) "Spent" else "Ausgegeben"
    fun remaining(lang: AppLanguage) = if (lang.isEnglish) "Remaining" else "Verbleibend"
    fun dailyAllowance(lang: AppLanguage) = if (lang.isEnglish) "Daily Allowance" else "Tagesbudget"
    fun adjustBudget(lang: AppLanguage) = if (lang.isEnglish) "Adjust Budget" else "Budget anpassen"

    // Statistics Screen
    fun monthlyAverage(lang: AppLanguage) = if (lang.isEnglish) "Monthly Average" else "Monatsdurchschnitt"
    fun highestExpense(lang: AppLanguage) = if (lang.isEnglish) "Highest Expense" else "Höchste Ausgabe"
    fun categoryBreakdown(lang: AppLanguage) = if (lang.isEnglish) "Category Breakdown" else "Kategorien-Übersicht"
    fun emotionAnalysis(lang: AppLanguage) = if (lang.isEnglish) "Emotional Spending Analysis" else "Konsumempfindungs-Analyse"

    // Map Screen
    fun mapExpenses(lang: AppLanguage) = if (lang.isEnglish) "Expenses on Map" else "Ausgaben auf der Karte"
    fun mostExpensivePlaces(lang: AppLanguage) = if (lang.isEnglish) "Most Expensive Places" else "Teuerste Orte"
    fun mapPinsExplanation(lang: AppLanguage) =
        if (lang.isEnglish) "Red circles scaled proportionally to amount"
        else "Rote Kreise proportional zur Ausgabenhöhe"
}
