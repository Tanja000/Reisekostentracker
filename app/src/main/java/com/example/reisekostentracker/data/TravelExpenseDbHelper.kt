package com.example.reisekostentracker.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.reisekostentracker.model.Expense
import com.example.reisekostentracker.model.ExpenseCategory
import com.example.reisekostentracker.model.Trip
import java.util.UUID

class TravelExpenseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "reisekosten.db"
        const val DATABASE_VERSION = 2

        // Table Categories
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_ICON = "icon_name"
        const val COL_CAT_COLOR = "color_value"
        const val COL_CAT_IS_DEFAULT = "is_default"

        // Table Trips
        const val TABLE_TRIPS = "trips"
        const val COL_TRIP_ID = "id"
        const val COL_TRIP_NAME = "name"
        const val COL_TRIP_DESTINATION = "destination"
        const val COL_TRIP_START_DATE = "start_date"
        const val COL_TRIP_END_DATE = "end_date"
        const val COL_TRIP_BUDGET = "budget"
        const val COL_TRIP_CURRENCY = "currency"
        const val COL_TRIP_IS_DEFAULT = "is_default"

        // Table Expenses
        const val TABLE_EXPENSES = "expenses"
        const val COL_EXP_ID = "id"
        const val COL_EXP_TITLE = "title"
        const val COL_EXP_AMOUNT = "amount"
        const val COL_EXP_CURRENCY = "currency"
        const val COL_EXP_CAT_ID = "category_id"
        const val COL_EXP_TRIP_ID = "trip_id"
        const val COL_EXP_DATE = "date"
        const val COL_EXP_NOTES = "notes"
        const val COL_EXP_PAYMENT_METHOD = "payment_method"
        const val COL_EXP_LAT = "latitude"
        const val COL_EXP_LNG = "longitude"
        const val COL_EXP_LOC_NAME = "location_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID TEXT PRIMARY KEY,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_ICON TEXT NOT NULL,
                $COL_CAT_COLOR INTEGER NOT NULL,
                $COL_CAT_IS_DEFAULT INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_TRIPS (
                $COL_TRIP_ID TEXT PRIMARY KEY,
                $COL_TRIP_NAME TEXT NOT NULL,
                $COL_TRIP_DESTINATION TEXT,
                $COL_TRIP_START_DATE INTEGER NOT NULL,
                $COL_TRIP_END_DATE INTEGER NOT NULL,
                $COL_TRIP_BUDGET REAL,
                $COL_TRIP_CURRENCY TEXT NOT NULL DEFAULT 'EUR',
                $COL_TRIP_IS_DEFAULT INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXP_ID TEXT PRIMARY KEY,
                $COL_EXP_TITLE TEXT NOT NULL,
                $COL_EXP_AMOUNT REAL NOT NULL,
                $COL_EXP_CURRENCY TEXT NOT NULL DEFAULT 'EUR',
                $COL_EXP_CAT_ID TEXT NOT NULL,
                $COL_EXP_TRIP_ID TEXT,
                $COL_EXP_DATE INTEGER NOT NULL,
                $COL_EXP_NOTES TEXT,
                $COL_EXP_PAYMENT_METHOD TEXT NOT NULL DEFAULT 'Karte',
                $COL_EXP_LAT REAL,
                $COL_EXP_LNG REAL,
                $COL_EXP_LOC_NAME TEXT
            )
            """.trimIndent()
        )

        // Seed default categories
        ExpenseCategory.defaultCategories.forEach { cat ->
            val values = ContentValues().apply {
                put(COL_CAT_ID, cat.id)
                put(COL_CAT_NAME, cat.name)
                put(COL_CAT_ICON, cat.iconName)
                put(COL_CAT_COLOR, cat.colorValue)
                put(COL_CAT_IS_DEFAULT, if (cat.isDefault) 1 else 0)
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COL_EXP_LAT REAL")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COL_EXP_LNG REAL")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COL_EXP_LOC_NAME TEXT")
        }
    }

    // Categories
    fun getAllCategories(): List<ExpenseCategory> {
        val list = mutableListOf<ExpenseCategory>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, "$COL_CAT_IS_DEFAULT DESC, $COL_CAT_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ExpenseCategory(
                        id = it.getString(it.getColumnIndexOrThrow(COL_CAT_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_CAT_NAME)),
                        iconName = it.getString(it.getColumnIndexOrThrow(COL_CAT_ICON)),
                        colorValue = it.getLong(it.getColumnIndexOrThrow(COL_CAT_COLOR)),
                        isDefault = it.getInt(it.getColumnIndexOrThrow(COL_CAT_IS_DEFAULT)) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertCategory(cat: ExpenseCategory) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_ID, cat.id)
            put(COL_CAT_NAME, cat.name)
            put(COL_CAT_ICON, cat.iconName)
            put(COL_CAT_COLOR, cat.colorValue)
            put(COL_CAT_IS_DEFAULT, if (cat.isDefault) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateCategory(cat: ExpenseCategory) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, cat.name)
            put(COL_CAT_ICON, cat.iconName)
            put(COL_CAT_COLOR, cat.colorValue)
        }
        db.update(TABLE_CATEGORIES, values, "$COL_CAT_ID = ?", arrayOf(cat.id))
    }

    fun deleteCategory(id: String) {
        val db = writableDatabase
        db.delete(TABLE_CATEGORIES, "$COL_CAT_ID = ? AND $COL_CAT_IS_DEFAULT = 0", arrayOf(id))
    }

    // Trips
    fun getAllTrips(): List<Trip> {
        val list = mutableListOf<Trip>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TRIPS, null, null, null, null, null, "$COL_TRIP_START_DATE DESC")
        cursor.use {
            while (it.moveToNext()) {
                val budgetIndex = it.getColumnIndexOrThrow(COL_TRIP_BUDGET)
                val budget = if (it.isNull(budgetIndex)) null else it.getDouble(budgetIndex)
                list.add(
                    Trip(
                        id = it.getString(it.getColumnIndexOrThrow(COL_TRIP_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_TRIP_NAME)),
                        destination = it.getString(it.getColumnIndexOrThrow(COL_TRIP_DESTINATION)) ?: "",
                        startDate = it.getLong(it.getColumnIndexOrThrow(COL_TRIP_START_DATE)),
                        endDate = it.getLong(it.getColumnIndexOrThrow(COL_TRIP_END_DATE)),
                        budget = budget,
                        currency = it.getString(it.getColumnIndexOrThrow(COL_TRIP_CURRENCY)) ?: "EUR",
                        isDefault = it.getInt(it.getColumnIndexOrThrow(COL_TRIP_IS_DEFAULT)) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertTrip(trip: Trip) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TRIP_ID, trip.id)
            put(COL_TRIP_NAME, trip.name)
            put(COL_TRIP_DESTINATION, trip.destination)
            put(COL_TRIP_START_DATE, trip.startDate)
            put(COL_TRIP_END_DATE, trip.endDate)
            put(COL_TRIP_BUDGET, trip.budget)
            put(COL_TRIP_CURRENCY, trip.currency)
            put(COL_TRIP_IS_DEFAULT, if (trip.isDefault) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_TRIPS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateTrip(trip: Trip) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TRIP_NAME, trip.name)
            put(COL_TRIP_DESTINATION, trip.destination)
            put(COL_TRIP_START_DATE, trip.startDate)
            put(COL_TRIP_END_DATE, trip.endDate)
            put(COL_TRIP_BUDGET, trip.budget)
            put(COL_TRIP_CURRENCY, trip.currency)
        }
        db.update(TABLE_TRIPS, values, "$COL_TRIP_ID = ?", arrayOf(trip.id))
    }

    fun deleteTrip(id: String) {
        val db = writableDatabase
        db.delete(TABLE_TRIPS, "$COL_TRIP_ID = ?", arrayOf(id))
    }

    // Expenses
    fun getAllExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.query(TABLE_EXPENSES, null, null, null, null, null, "$COL_EXP_DATE DESC")
        cursor.use {
            while (it.moveToNext()) {
                val latIndex = it.getColumnIndexOrThrow(COL_EXP_LAT)
                val lngIndex = it.getColumnIndexOrThrow(COL_EXP_LNG)
                val locIndex = it.getColumnIndexOrThrow(COL_EXP_LOC_NAME)

                val lat = if (it.isNull(latIndex)) null else it.getDouble(latIndex)
                val lng = if (it.isNull(lngIndex)) null else it.getDouble(lngIndex)
                val locName = if (it.isNull(locIndex)) null else it.getString(locIndex)

                list.add(
                    Expense(
                        id = it.getString(it.getColumnIndexOrThrow(COL_EXP_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COL_EXP_TITLE)),
                        amount = it.getDouble(it.getColumnIndexOrThrow(COL_EXP_AMOUNT)),
                        currency = it.getString(it.getColumnIndexOrThrow(COL_EXP_CURRENCY)) ?: "EUR",
                        categoryId = it.getString(it.getColumnIndexOrThrow(COL_EXP_CAT_ID)),
                        tripId = it.getString(it.getColumnIndexOrThrow(COL_EXP_TRIP_ID)),
                        date = it.getLong(it.getColumnIndexOrThrow(COL_EXP_DATE)),
                        notes = it.getString(it.getColumnIndexOrThrow(COL_EXP_NOTES)) ?: "",
                        paymentMethod = it.getString(it.getColumnIndexOrThrow(COL_EXP_PAYMENT_METHOD)) ?: "Karte",
                        latitude = lat,
                        longitude = lng,
                        locationName = locName
                    )
                )
            }
        }
        return list
    }

    fun insertExpense(expense: Expense) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXP_ID, expense.id)
            put(COL_EXP_TITLE, expense.title)
            put(COL_EXP_AMOUNT, expense.amount)
            put(COL_EXP_CURRENCY, expense.currency)
            put(COL_EXP_CAT_ID, expense.categoryId)
            put(COL_EXP_TRIP_ID, expense.tripId)
            put(COL_EXP_DATE, expense.date)
            put(COL_EXP_NOTES, expense.notes)
            put(COL_EXP_PAYMENT_METHOD, expense.paymentMethod)
            put(COL_EXP_LAT, expense.latitude)
            put(COL_EXP_LNG, expense.longitude)
            put(COL_EXP_LOC_NAME, expense.locationName)
        }
        db.insertWithOnConflict(TABLE_EXPENSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateExpense(expense: Expense) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXP_TITLE, expense.title)
            put(COL_EXP_AMOUNT, expense.amount)
            put(COL_EXP_CURRENCY, expense.currency)
            put(COL_EXP_CAT_ID, expense.categoryId)
            put(COL_EXP_TRIP_ID, expense.tripId)
            put(COL_EXP_DATE, expense.date)
            put(COL_EXP_NOTES, expense.notes)
            put(COL_EXP_PAYMENT_METHOD, expense.paymentMethod)
            put(COL_EXP_LAT, expense.latitude)
            put(COL_EXP_LNG, expense.longitude)
            put(COL_EXP_LOC_NAME, expense.locationName)
        }
        db.update(TABLE_EXPENSES, values, "$COL_EXP_ID = ?", arrayOf(expense.id))
    }

    fun deleteExpense(id: String) {
        val db = writableDatabase
        db.delete(TABLE_EXPENSES, "$COL_EXP_ID = ?", arrayOf(id))
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_EXPENSES, null, null)
    }
}
