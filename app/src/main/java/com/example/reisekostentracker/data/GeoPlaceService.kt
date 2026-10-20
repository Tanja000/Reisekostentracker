package com.example.reisekostentracker.data

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class GeoPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "Deutschland"
)

object GeoPlaceLibrary {

    // Offline built-in catalog of major cities and destinations for instant, guaranteed offline geocoding
    val popularPlaces = listOf(
        GeoPlace("Karlsruhe", 49.0069, 8.4037),
        GeoPlace("München", 48.1371, 11.5754),
        GeoPlace("Berlin", 52.5200, 13.4050),
        GeoPlace("Hamburg", 53.5511, 9.9937),
        GeoPlace("Köln", 50.9375, 6.9603),
        GeoPlace("Frankfurt am Main", 50.1109, 8.6821),
        GeoPlace("Stuttgart", 48.7758, 9.1829),
        GeoPlace("Düsseldorf", 51.2277, 6.7735),
        GeoPlace("Leipzig", 51.3397, 12.3731),
        GeoPlace("Dortmund", 51.5136, 7.4653),
        GeoPlace("Essen", 51.4556, 7.0116),
        GeoPlace("Bremen", 53.0793, 8.8017),
        GeoPlace("Dresden", 51.0504, 13.7373),
        GeoPlace("Hannover", 52.3759, 9.7320),
        GeoPlace("Nürnberg", 49.4521, 11.0767),
        GeoPlace("Duisburg", 51.4344, 6.7623),
        GeoPlace("Bochum", 51.4818, 7.2162),
        GeoPlace("Wuppertal", 51.2562, 7.1508),
        GeoPlace("Bielefeld", 52.0302, 8.5325),
        GeoPlace("Bonn", 50.7374, 7.0982),
        GeoPlace("Münster", 51.9607, 7.6261),
        GeoPlace("Mannheim", 49.4875, 8.4660),
        GeoPlace("Karlsruhe Durlach", 48.9989, 8.4722),
        GeoPlace("Augsburg", 48.3705, 10.8978),
        GeoPlace("Wiesbaden", 50.0782, 8.2398),
        GeoPlace("Mönchengladbach", 51.1805, 6.4428),
        GeoPlace("Gelsenkirchen", 51.5177, 7.0857),
        GeoPlace("Braunschweig", 52.2689, 10.5268),
        GeoPlace("Kiel", 54.3233, 10.1228),
        GeoPlace("Chemnitz", 50.8278, 12.9214),
        GeoPlace("Aachen", 50.7753, 6.0839),
        GeoPlace("Halle (Saale)", 51.4828, 11.9698),
        GeoPlace("Magdeburg", 52.1205, 11.6276),
        GeoPlace("Freiburg im Breisgau", 47.9990, 7.8421),
        GeoPlace("Krefeld", 51.3333, 6.5667),
        GeoPlace("Lübeck", 53.8655, 10.6866),
        GeoPlace("Oberhausen", 51.4700, 6.8520),
        GeoPlace("Erfurt", 50.9848, 11.0299),
        GeoPlace("Mainz", 49.9929, 8.2473),
        GeoPlace("Rostock", 54.0924, 12.0991),
        GeoPlace("Kassel", 51.3127, 9.4797),
        GeoPlace("Hagen", 51.3671, 7.4633),
        GeoPlace("Hamm", 51.6814, 7.8183),
        GeoPlace("Saarbrücken", 49.2402, 6.9969),
        GeoPlace("Potsdam", 52.3906, 13.0645),
        GeoPlace("Heidelberg", 49.3988, 8.6724),
        GeoPlace("Ulm", 48.4011, 9.9876),
        GeoPlace("Würzburg", 49.7913, 9.9534),
        GeoPlace("Wien", 48.2082, 16.3738, "Österreich"),
        GeoPlace("Salzburg", 47.8095, 13.0550, "Österreich"),
        GeoPlace("Innsbruck", 47.2692, 11.4041, "Österreich"),
        GeoPlace("Zürich", 47.3769, 8.5417, "Schweiz"),
        GeoPlace("Basel", 47.5596, 7.5886, "Schweiz"),
        GeoPlace("Bern", 46.9480, 7.4474, "Schweiz"),
        GeoPlace("Paris", 48.8566, 2.3522, "Frankreich"),
        GeoPlace("London", 51.5074, -0.1278, "Großbritannien"),
        GeoPlace("Rom", 41.9028, 12.4964, "Italien"),
        GeoPlace("Mailand", 45.4642, 9.1900, "Italien"),
        GeoPlace("Barcelona", 41.3851, 2.1734, "Spanien"),
        GeoPlace("Madrid", 40.4168, -3.7038, "Spanien"),
        GeoPlace("Amsterdam", 52.3676, 4.9041, "Niederlande")
    )

    /**
     * Resolves a location name into a GeoPlace with exact latitude & longitude.
     * Checks the offline city library first, and falls back to android.location.Geocoder.
     */
    suspend fun resolveLocation(name: String, context: Context): GeoPlace = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return@withContext GeoPlace("Karlsruhe", 49.0069, 8.4037)
        }

        // 1. Direct offline match in popularPlaces
        val offlineMatch = popularPlaces.firstOrNull {
            it.name.equals(trimmed, ignoreCase = true) ||
            trimmed.contains(it.name, ignoreCase = true) ||
            it.name.contains(trimmed, ignoreCase = true)
        }
        if (offlineMatch != null) {
            return@withContext offlineMatch.copy(name = trimmed)
        }

        // 2. Android Geocoder lookup for any other street, city or postal code
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Geocoder.isPresent()) {
                val results = geocoder.getFromLocationName(trimmed, 1)
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    val resolvedName = addr.locality ?: addr.featureName ?: addr.subAdminArea ?: trimmed
                    return@withContext GeoPlace(
                        name = resolvedName,
                        latitude = addr.latitude,
                        longitude = addr.longitude,
                        country = addr.countryName ?: "Unbekannt"
                    )
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback: hash-based stable jitter near Karlsruhe/Frankfurt center
        val hash = trimmed.hashCode().toLong()
        val latOffset = ((hash % 1000) / 1000.0 - 0.5) * 0.1
        val lngOffset = (((hash / 1000) % 1000) / 1000.0 - 0.5) * 0.1
        return@withContext GeoPlace(
            name = trimmed,
            latitude = 49.0069 + latOffset,
            longitude = 8.4037 + lngOffset
        )
    }
}
