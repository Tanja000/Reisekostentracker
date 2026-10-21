package com.example.reisekostentracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ExpenseCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val colorValue: Long,
    val isDefault: Boolean = false
) {
    val color: Color
        get() = Color(colorValue)

    val icon: ImageVector
        get() = getIconByName(iconName)

    fun localizedName(isEn: Boolean): String {
        if (!isEn) return name
        return when (id) {
            "cat_lebensmittel" -> "Groceries & Supermarket"
            "cat_wohnen" -> "Rent & Housing"
            "cat_gastronomie" -> "Restaurant & Café"
            "cat_transport" -> "Mobility & Transport"
            "cat_shopping" -> "Shopping & Fashion"
            "cat_freizeit" -> "Leisure & Hobby"
            "cat_gesundheit" -> "Health & Drugstore"
            "cat_abos" -> "Contracts & Subscriptions"
            "cat_reisen" -> "Vacation & Travel"
            "cat_sonstiges" -> "Miscellaneous"
            else -> when (name) {
                "Lebensmittel & Supermarkt" -> "Groceries & Supermarket"
                "Miete & Wohnen" -> "Rent & Housing"
                "Restaurant & Café" -> "Restaurant & Café"
                "Mobilität & Transport" -> "Mobility & Transport"
                "Shopping & Mode" -> "Shopping & Fashion"
                "Freizeit & Hobby" -> "Leisure & Hobby"
                "Gesundheit & Drogerie" -> "Health & Drugstore"
                "Verträge & Abos" -> "Contracts & Subscriptions"
                "Urlaub & Reisen" -> "Vacation & Travel"
                "Sonstiges" -> "Miscellaneous"
                else -> name
            }
        }
    }

    companion object {
        fun getIconByName(name: String): ImageVector {
            return when (name.lowercase()) {
                "hotel", "unterkunft", "bed" -> Icons.Default.Hotel
                "apartment" -> Icons.Default.Apartment
                "home" -> Icons.Default.Home
                "restaurant", "essen" -> Icons.Default.Restaurant
                "local_cafe", "cafe" -> Icons.Default.LocalCafe
                "fastfood" -> Icons.Default.Fastfood
                "local_bar", "bar" -> Icons.Default.LocalBar
                "flight", "flug" -> Icons.Default.Flight
                "directions_car", "car", "auto" -> Icons.Default.DirectionsCar
                "train", "zug", "bahn" -> Icons.Default.Train
                "directions_bus", "bus" -> Icons.Default.DirectionsBus
                "directions_boat", "schiff", "faehre" -> Icons.Default.DirectionsBoat
                "pedal_bike", "fahrrad" -> Icons.Default.PedalBike
                "two_wheeler", "motorrad", "roller" -> Icons.Default.TwoWheeler
                "local_gas_station", "tanken" -> Icons.Default.LocalGasStation
                "local_activity", "ticket" -> Icons.Default.LocalActivity
                "museum" -> Icons.Default.Museum
                "attractions", "sehenswuerdigkeit" -> Icons.Default.Attractions
                "beach_access", "strand", "urlaub" -> Icons.Default.BeachAccess
                "pool", "schwimmen" -> Icons.Default.Pool
                "hiking", "wandern" -> Icons.Default.Hiking
                "sports_soccer", "sport" -> Icons.Default.SportsSoccer
                "fitness_center", "gym" -> Icons.Default.FitnessCenter
                "spa", "wellness" -> Icons.Default.Spa
                "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
                "shopping_cart", "supermarkt" -> Icons.Default.ShoppingCart
                "store" -> Icons.Default.Store
                "card_giftcard", "geschenk" -> Icons.Default.CardGiftcard
                "local_pharmacy", "apotheke" -> Icons.Default.LocalPharmacy
                "medical_services", "arzt" -> Icons.Default.MedicalServices
                "payments", "geld" -> Icons.Default.Payments
                "credit_card", "karte" -> Icons.Default.CreditCard
                "account_balance", "bank" -> Icons.Default.AccountBalance
                "photo_camera", "kamera", "foto" -> Icons.Default.PhotoCamera
                "luggage", "koffer" -> Icons.Default.Luggage
                "pets", "haustier" -> Icons.Default.Pets
                "park", "natur" -> Icons.Default.Park
                "theater_comedy", "kultur" -> Icons.Default.TheaterComedy
                "movie", "kino" -> Icons.Default.Movie
                "vpn_key", "schluessel", "miete" -> Icons.Default.VpnKey
                "local_laundry_service", "waesche" -> Icons.Default.LocalLaundryService
                else -> Icons.Default.Category
            }
        }

        val availableIcons = listOf(
            "hotel" to "Hotel",
            "apartment" to "Ferienwohnung",
            "home" to "Unterkunft",
            "restaurant" to "Restaurant",
            "local_cafe" to "Café & Snack",
            "fastfood" to "Fast Food",
            "local_bar" to "Bar & Drinks",
            "flight" to "Flug",
            "directions_car" to "Mietwagen / Auto",
            "train" to "Zug / Bahn",
            "directions_bus" to "Bus / Transfer",
            "directions_boat" to "Schiff / Fähre",
            "pedal_bike" to "Fahrrad / E-Bike",
            "two_wheeler" to "Motorrad / Roller",
            "local_gas_station" to "Benzin / Tanken",
            "local_activity" to "Events / Tickets",
            "museum" to "Museum / Kultur",
            "attractions" to "Sightseeing",
            "beach_access" to "Strand / Relax",
            "pool" to "Pool & Schwimmen",
            "hiking" to "Wandern & Natur",
            "sports_soccer" to "Sport",
            "fitness_center" to "Gym",
            "spa" to "Wellness & Spa",
            "shopping_bag" to "Shopping & Mode",
            "shopping_cart" to "Supermarkt & Vorrat",
            "store" to "Geschäfte",
            "card_giftcard" to "Souvenirs & Geschenke",
            "local_pharmacy" to "Apotheke",
            "medical_services" to "Arzt / Notfall",
            "payments" to "Bargeld / Gebühren",
            "credit_card" to "Kartengebühren",
            "account_balance" to "Visum / Bank",
            "photo_camera" to "Fotografie / Tour",
            "luggage" to "Gepäck / Koffer",
            "pets" to "Haustier / Reise",
            "park" to "Nationalpark",
            "theater_comedy" to "Theater / Show",
            "movie" to "Kino / Entertainment",
            "vpn_key" to "Miete & Kaution",
            "local_laundry_service" to "Wäscheservice",
            "category" to "Sonstiges"
        )

        val defaultColors = listOf(
            0xFF3F51B5L, // Indigo
            0xFFFF9800L, // Orange
            0xFF009688L, // Teal
            0xFF4CAF50L, // Green
            0xFFE91E63L, // Pink
            0xFF9C27B0L, // Purple
            0xFF2196F3L, // Blue
            0xFFFF5722L, // Deep Orange
            0xFF795548L, // Brown
            0xFF607D8BL, // Slate
            0xFF00BCD4L, // Cyan
            0xFFFFC107L, // Amber
            0xFFE040FBL, // Neon Purple
            0xFF00E676L, // Neon Green
            0xFFFF1744L, // Crimson
            0xFF3D5AFEL  // Electric Indigo
        )

        val defaultCategories = listOf(
            ExpenseCategory("cat_lebensmittel", "Lebensmittel & Supermarkt", "shopping_cart", 0xFF10B981L, isDefault = true),
            ExpenseCategory("cat_wohnen", "Miete & Wohnen", "home", 0xFF6366F1L, isDefault = true),
            ExpenseCategory("cat_gastronomie", "Restaurant & Café", "restaurant", 0xFFF59E0BL, isDefault = true),
            ExpenseCategory("cat_transport", "Mobilität & Transport", "directions_car", 0xFF06B6D4L, isDefault = true),
            ExpenseCategory("cat_shopping", "Shopping & Mode", "shopping_bag", 0xFFEC4899L, isDefault = true),
            ExpenseCategory("cat_freizeit", "Freizeit & Hobby", "sports_soccer", 0xFF8B5CF6L, isDefault = true),
            ExpenseCategory("cat_gesundheit", "Gesundheit & Drogerie", "medical_services", 0xFFEF4444L, isDefault = true),
            ExpenseCategory("cat_abos", "Verträge & Abos", "credit_card", 0xFF3B82F6L, isDefault = true),
            ExpenseCategory("cat_reisen", "Urlaub & Reisen", "flight", 0xFF14B8A6L, isDefault = true),
            ExpenseCategory("cat_sonstiges", "Sonstiges", "category", 0xFF64748BL, isDefault = true)
        )
    }
}
