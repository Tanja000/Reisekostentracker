package com.example.reisekostentracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow

data class CompoundResult(
    val initialAmount: Double,
    val years: Int,
    val nominalValue: Double,
    val realPurchasingPower: Double,
    val totalNominalGain: Double,
    val multiplier: Double,
    val inflationRatePercent: Double,
    val annualReturnPercent: Double
)

class WorldBankApiService {

    companion object {
        private const val WORLDBANK_GERMANY_INFLATION_URL =
            "https://api.worldbank.org/v2/country/DEU/indicator/FP.CPI.TOTL.ZG?format=json"
        
        // Historischer Durchschnitt Weltbank Deutschland Inflation
        const val DEFAULT_INFLATION_RATE = 2.25 // 2.25%
        // Historische Durchschnittsrendite weltweiter Aktienmarkt (z.B. MSCI World / S&P 500)
        const val DEFAULT_EQUITY_RETURN_RATE = 7.0 // 7.0%
    }

    private var cachedInflationRate: Double? = null

    /**
     * Ruft die offizielle Inflationsrate für Deutschland von der Weltbank API ab.
     * Nutzt Dispatchers.IO und puffert das Ergebnis.
     */
    suspend fun getGermanyInflationRate(forceRefresh: Boolean = false): Double = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedInflationRate?.let { return@withContext it }
        }

        try {
            val url = URL(WORLDBANK_GERMANY_INFLATION_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "TrackMyCent-Android")
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)
                if (jsonArray.length() >= 2) {
                    val dataRecords = jsonArray.getJSONArray(1)
                    for (i in 0 until dataRecords.length()) {
                        val record = dataRecords.getJSONObject(i)
                        if (!record.isNull("value")) {
                            val rate = record.getDouble("value")
                            if (rate in -5.0..25.0) {
                                cachedInflationRate = rate
                                return@withContext rate
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Offline oder Timeout: Sicherer Fallback auf Weltbank-Durchschnitt
        }

        cachedInflationRate = DEFAULT_INFLATION_RATE
        DEFAULT_INFLATION_RATE
    }

    /**
     * Berechnet Zinseszins und Kaufkraft nach Inflation über n Jahre.
     * Formel:
     * Nominal = K0 * (1 + r)^t
     * Real (Kaufkraft) = K0 * ((1 + r) / (1 + i))^t
     */
    fun calculateCompound(
        amount: Double,
        years: Int = 30,
        annualReturnRate: Double = DEFAULT_EQUITY_RETURN_RATE,
        inflationRate: Double = (cachedInflationRate ?: DEFAULT_INFLATION_RATE)
    ): CompoundResult {
        val r = annualReturnRate / 100.0
        val i = inflationRate / 100.0

        val nominalMultiplier = (1.0 + r).pow(years.toDouble())
        val nominalValue = amount * nominalMultiplier

        val realBase = (1.0 + r) / (1.0 + i)
        val realPurchasingPower = amount * realBase.pow(years.toDouble())

        return CompoundResult(
            initialAmount = amount,
            years = years,
            nominalValue = nominalValue,
            realPurchasingPower = realPurchasingPower,
            totalNominalGain = nominalValue - amount,
            multiplier = nominalMultiplier,
            inflationRatePercent = inflationRate,
            annualReturnPercent = annualReturnRate
        )
    }
}
