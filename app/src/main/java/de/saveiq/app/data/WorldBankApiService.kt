package de.saveiq.app.data

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
        private const val WORLDBANK_DEU_INFLATION_URL =
            "https://api.worldbank.org/v2/country/DEU/indicator/FP.CPI.TOTL.ZG?format=json"
        private const val WORLDBANK_GBR_INFLATION_URL =
            "https://api.worldbank.org/v2/country/GBR/indicator/FP.CPI.TOTL.ZG?format=json"

        // Neueste historische Richtwerte / Fallback Weltbank
        const val DEFAULT_INFLATION_RATE = 2.17 // 2.17% (Deutschland)
        const val DEFAULT_GBR_INFLATION_RATE = 3.88 // 3.88% (UK / England)
        // Historische Durchschnittsrendite weltweiter Aktienmarkt (MSCI World / S&P 500)
        const val DEFAULT_EQUITY_RETURN_RATE = 7.0 // 7.0%
    }

    private var cachedDeuInflationRate: Double? = null
    private var cachedGbrInflationRate: Double? = null

    /**
     * Ruft die tagesaktuelle, neueste Inflationsrate für Deutschland (DEU) oder England/UK (GBR)
     * direkt von der offiziellen Weltbank API ab.
     */
    suspend fun getInflationRate(
        countryCode: String = "DEU",
        forceRefresh: Boolean = false
    ): Double = withContext(Dispatchers.IO) {
        val isUk = countryCode.equals("GBR", true) || countryCode.equals("GB", true) || countryCode.equals("UK", true)
        if (!forceRefresh) {
            val cached = if (isUk) cachedGbrInflationRate else cachedDeuInflationRate
            if (cached != null) return@withContext cached
        }

        val urlStr = if (isUk) WORLDBANK_GBR_INFLATION_URL else WORLDBANK_DEU_INFLATION_URL
        val fallback = if (isUk) DEFAULT_GBR_INFLATION_RATE else DEFAULT_INFLATION_RATE

        try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "SaveIQ-Android")
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
                            if (rate in -5.0..30.0) {
                                if (isUk) {
                                    cachedGbrInflationRate = rate
                                } else {
                                    cachedDeuInflationRate = rate
                                }
                                return@withContext rate
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Offline oder Timeout: Sicherer Fallback auf Weltbank-Referenz
        }

        if (isUk) {
            cachedGbrInflationRate = fallback
        } else {
            cachedDeuInflationRate = fallback
        }
        fallback
    }

    suspend fun getGermanyInflationRate(forceRefresh: Boolean = false): Double =
        getInflationRate("DEU", forceRefresh)

    suspend fun getUkInflationRate(forceRefresh: Boolean = false): Double =
        getInflationRate("GBR", forceRefresh)

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
        inflationRate: Double = (cachedDeuInflationRate ?: DEFAULT_INFLATION_RATE)
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
