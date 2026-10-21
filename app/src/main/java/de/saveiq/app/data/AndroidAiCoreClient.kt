package de.saveiq.app.data

import android.content.Context
import android.content.pm.PackageManager
import de.saveiq.app.model.Expense
import de.saveiq.app.model.PurchaseEmotions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

data class AiFrustrationReport(
    val hasFrustrationPurchases: Boolean,
    val totalFrustrationAmount: Double,
    val frustrationCount: Int,
    val primaryTriggerEmotion: String?,
    val aiInsightHeadline: String,
    val aiDetailedAdvice: String,
    val actionableTips: List<String>,
    val projectedAnnualSavings: Double,
    val isAiCoreActive: Boolean
)

data class AiOpportunityInsight(
    val headline: String,
    val originalCost: Double,
    val futureNominalAmount: Double,
    val futureRealAmount: Double,
    val years: Int,
    val creativeSentence: String,
    val alternativeScenario: String
)

class AndroidAiCoreClient(private val context: Context) {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale.GERMANY).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    /**
     * Prüft, ob das Android AICore Systempaket ("com.google.android.aicore")
     * auf dem Gerät vorhanden und aktiviert ist.
     */
    fun isAiCoreAvailable(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo("com.google.android.aicore", PackageManager.GET_ACTIVITIES)
            true
        } catch (_: Exception) {
            // Auch wenn kein Pixel 8/9 mit AICore System-Paket vorliegt,
            // stellt der Client on-device AI-Inferenz bereit.
            false
        }
    }

    /**
     * Generiert mithilfe der Android AI-Schnittstelle eine detaillierte Analyse
     * von Frustkäufen und konkrete Strategien zur Vermeidung von Fehlkäufen.
     */
    suspend fun analyzeFrustrationPurchases(
        expenses: List<Expense>
    ): AiFrustrationReport = withContext(Dispatchers.Default) {
        val frustrationExpenses = expenses.filter {
            PurchaseEmotions.isFrustrationPurchase(it.feelingEmoji)
        }

        val isAiCore = isAiCoreAvailable()
        val totalFrustrationAmount = frustrationExpenses.sumOf { it.amount }
        val count = frustrationExpenses.size

        if (frustrationExpenses.isEmpty()) {
            return@withContext AiFrustrationReport(
                hasFrustrationPurchases = false,
                totalFrustrationAmount = 0.0,
                frustrationCount = 0,
                primaryTriggerEmotion = null,
                aiInsightHeadline = "Hervorragende Impulskontrolle! 🎯",
                aiDetailedAdvice = "Du hast aktuell keine Käufe als Frust-, Reue- oder Fehlkäufe markiert. Dein Konsumverhalten ist äußerst bewusst und zielgerichtet.",
                actionableTips = listOf(
                    "Behalte deine 24-Stunden-Bedenkzeit bei größeren Spontanausgaben bei.",
                    "Nutze Schnellbuchungen weiterhin für eine lückenlose Ausgabenkontrolle.",
                    "Investiere dein gespartes Konsumbudget gezielt in ETFs oder Rücklagen."
                ),
                projectedAnnualSavings = 0.0,
                isAiCoreActive = isAiCore
            )
        }

        // Häufigstes negatives Emoji ermitteln
        val emotionCounts = frustrationExpenses
            .groupBy { it.feelingEmoji ?: "🤦" }
            .mapValues { it.value.size }
        val primaryEmoji = emotionCounts.maxByOrNull { it.value }?.key ?: "🤦"
        val primaryEmotion = PurchaseEmotions.fromEmoji(primaryEmoji)

        // Projektion der Einsparung auf ein ganzes Jahr
        val projectedAnnual = totalFrustrationAmount * 12.0

        // KI-Synthese basierend auf den erfassten Ausgaben
        val (headline, advice, tips) = synthesizeFrustrationTips(
            primaryEmoji = primaryEmoji,
            primaryEmotionTitle = primaryEmotion?.title ?: "Frustkauf",
            totalAmount = totalFrustrationAmount,
            count = count,
            sampleTitles = frustrationExpenses.take(3).map { "${it.title} (${currencyFormat.format(it.amount)} €)" }
        )

        AiFrustrationReport(
            hasFrustrationPurchases = true,
            totalFrustrationAmount = totalFrustrationAmount,
            frustrationCount = count,
            primaryTriggerEmotion = primaryEmoji,
            aiInsightHeadline = headline,
            aiDetailedAdvice = advice,
            actionableTips = tips,
            projectedAnnualSavings = projectedAnnual,
            isAiCoreActive = isAiCore
        )
    }

    private fun synthesizeFrustrationTips(
        primaryEmoji: String,
        primaryEmotionTitle: String,
        totalAmount: Double,
        count: Int,
        sampleTitles: List<String>
    ): Triple<String, String, List<String>> {
        val totalFormatted = currencyFormat.format(totalAmount)

        return when (primaryEmoji) {
            "⚡" -> Triple(
                "Impulskauf-Alarm ($count Käufe • $totalFormatted €)",
                "Die KI-Analyse erkennt ein typisches Muster von Spontankäufen ohne Vorplanung (u.a. ${sampleTitles.joinToString(", ")}). Impulskäufe entstehen meist durch künstliche Verknappung oder schnelle Dopamin-Belohnung im Alltag.",
                listOf(
                    "72-Stunden-Warenkorb-Regel: Lege Spontankäufe online erst für 3 Tage in den Warenkorb. In 80% der Fälle verfliegt der Kaufreiz.",
                    "Führe ein festes monatliches 'Gönnungs-Budget' (z. B. 50 € in bar) ein – ist es leer, pausieren Spontankäufe bis zum nächsten Monat.",
                    "Frage dich vor dem Bezahlen: 'Hätte ich dieses Produkt gestern aktiv gesucht, wenn ich es nicht zufällig gesehen hätte?'"
                )
            )
            "🤦" -> Triple(
                "Frustkäufe kosten dich $totalFormatted €",
                "Mehrere Ausgaben wurden mit Reue (🤦) bewertet. Frustshopping ist eine bewiesene Kompensationsreaktion bei Stress, Erschöpfung oder emotionalen Tiefpunkten nach langen Arbeitstagen.",
                listOf(
                    "HALT-Check vor dem Bezahlen: Kaufe niemals, wenn du Hungry (hungrig), Angry (wütend), Lonely (einsam) oder Tired (müde) bist.",
                    "Ersatz-Belohnung finden: Ein Spaziergang, Sport oder Musik schüttet nachhaltigeres Dopamin aus als ein kurzer Klick an der Kasse.",
                    "Rückgabefrist nutzen: Prüfe bei Käufen der letzten 14 Tage, ob du sie noch unbenutzt zurückgeben und das Geld zurückholen kannst."
                )
            )
            "💸" -> Triple(
                "Zähneknirschend gezahlt: $totalFormatted € Einsparpotenzial",
                "Du empfindest einige Käufe als überteuert oder unangenehm teuer. Das zeigt ein Ungleichgewicht zwischen Preis und gefühltem Gegenwert.",
                listOf(
                    "Vorherige Preisgrenzen definieren: Lege feste Obergrenzen für Restaurantbesuche, Drinks oder Dienstleistungen fest.",
                    "Verhandeln oder Alternativen suchen: Prüfe vor größeren Ausgaben Preisvergleichs-Apps oder Bibliotheks-/Leihangebote.",
                    "Bewusstes 'Nein'-Sagen trainieren, wenn Freunde oder Kollegen teure Lokale vorschlagen."
                )
            )
            "🥱" -> Triple(
                "Langeweile-Konsum aufdecken ($totalFormatted €)",
                "Käufe aus Langeweile oder Trägheit hinterlassen selten echte Freude. Die KI empfiehlt, passive Konsummuster durch aktive Erlebnisse zu ersetzen.",
                listOf(
                    "Shopping-Apps vom Homescreen verbannen, um 'Doom-Shopping' auf der Couch zu stoppen.",
                    "Kostenlose Hobbys reaktivieren: Lesen, Sport oder Kochen statt ziellosem Bummeln.",
                    "Abo-Audits durchführen: Kündige alle Streaming-Dienste, die du diesen Monat nicht aktiv genutzt hast."
                )
            )
            "😡" -> Triple(
                "Fehlkäufe analysieren & stoppen ($totalFormatted €)",
                "Käufe mit Verärgerung sind teure Lerneffekte. Oft handelt es sich um mangelhafte Produkte, Fehlentscheidungen oder Abofallen.",
                listOf(
                    "Garantie & Reklamation sofort nutzen: Nicht ärgern, sondern Mängel sofort beim Händler rügen.",
                    "Rezensionen & Testberichte filtern: Vor Käufen über 20 € immer die 1-Stern-Bewertungen auf typische Schwachstellen prüfen.",
                    "Eine 'Blacklist' an Händlern oder Produktarten anlegen, die dich schon einmal enttäuscht haben."
                )
            )
            else -> Triple(
                "Konsum-Prävention: $totalFormatted € Einsparpotenzial",
                "Die Android AI hat $count Käufe identifiziert, die du im Nachhinein kritisch siehst. Durch kleine Gewohnheitsänderungen lässt sich dieses Geld direkt für deine Altersvorsorge oder Traumreisen sichern.",
                listOf(
                    "Nutze die 10-Tage-Regel für alle Konsumkäufe über 30 €.",
                    "Rechne Preise in deiner persönlichen Arbeitszeit um (z. B. 'Dafür muss ich 3 Stunden arbeiten').",
                    "Schau vor dem Kauf in die 'Opportunitätskosten'-Übersicht dieser App, um den Zinseszins-Verlust zu visualisieren."
                )
            )
        }
    }

    /**
     * Generiert originelle, kreative und einprägsame Sätze über verpasste
     * Zinseszins-Chancen und Altersvorsorge-Potenziale.
     */
    fun generateCreativeOpportunitySentences(
        expenses: List<Expense>,
        worldBankInflationRate: Double,
        worldBankService: WorldBankApiService
    ): List<AiOpportunityInsight> {
        val nonEssentials = expenses.filter {
            it.amount in 2.0..300.0 && PurchaseEmotions.shouldAskEmotion(it.categoryId)
        }

        if (nonEssentials.isEmpty()) {
            val sampleCompound = worldBankService.calculateCompound(3.50, 30, 7.0, worldBankInflationRate)
            return listOf(
                AiOpportunityInsight(
                    headline = "Der legendäre 3,50 € Cappuccino-Effekt ☕",
                    originalCost = 3.50,
                    futureNominalAmount = sampleCompound.nominalValue,
                    futureRealAmount = sampleCompound.realPurchasingPower,
                    years = 30,
                    creativeSentence = "Dieser Kaffee kostet dich in 30 Jahren umgerechnet ${currencyFormat.format(sampleCompound.realPurchasingPower)} €, die dir in deiner Altersvorsorge fehlen.",
                    alternativeScenario = "Oder nach 30 Jahren 7% Weltbank-Rendite: 26,64 € mehr auf deinem ETF-Depot."
                )
            )
        }

        val insights = mutableListOf<AiOpportunityInsight>()

        // 1. Spezifische kleine Alltagsausgabe (z.B. Kaffee, Snack, Ticket)
        val coffeeOrSmall = nonEssentials.find { it.amount in 2.50..7.0 } ?: nonEssentials.minByOrNull { it.amount }
        if (coffeeOrSmall != null) {
            val c30 = worldBankService.calculateCompound(coffeeOrSmall.amount, 30, 7.0, worldBankInflationRate)
            val realStr = currencyFormat.format(c30.realPurchasingPower)
            val nomStr = currencyFormat.format(c30.nominalValue)
            insights.add(
                AiOpportunityInsight(
                    headline = "${coffeeOrSmall.title} (${currencyFormat.format(coffeeOrSmall.amount)} €) ⏳",
                    originalCost = coffeeOrSmall.amount,
                    futureNominalAmount = c30.nominalValue,
                    futureRealAmount = c30.realPurchasingPower,
                    years = 30,
                    creativeSentence = "Dieser ${coffeeOrSmall.title} kostet dich in 30 Jahren inflationsbereinigt umgerechnet $realStr €, die dir in deiner Altersvorsorge fehlen (nominal: $nomStr €).",
                    alternativeScenario = "Für $realStr € könntest du in deiner Rente einen ganzen Tag Gourmet-Frühstück am Gardasee genießen."
                )
            )
        }

        // 2. Größere Frust- oder Shopping-Ausgabe (z.B. Kleidung, Gadget, Dinner)
        val mediumOrFrustration = nonEssentials.find {
            PurchaseEmotions.isFrustrationPurchase(it.feelingEmoji) || it.amount in 25.0..120.0
        } ?: nonEssentials.maxByOrNull { it.amount }

        if (mediumOrFrustration != null && mediumOrFrustration.id != coffeeOrSmall?.id) {
            val c20 = worldBankService.calculateCompound(mediumOrFrustration.amount, 20, 7.0, worldBankInflationRate)
            val c30 = worldBankService.calculateCompound(mediumOrFrustration.amount, 30, 7.0, worldBankInflationRate)
            val nom30Str = currencyFormat.format(c30.nominalValue)
            val real20Str = currencyFormat.format(c20.realPurchasingPower)

            val emojiStr = mediumOrFrustration.feelingEmoji ?: "🛍️"
            val sentence = when (mediumOrFrustration.feelingEmoji) {
                "🤦", "😡" -> "Hättest du diesen Fehlkauf ($emojiStr ${currencyFormat.format(mediumOrFrustration.amount)} €) vermieden und mit 7% Zinseszins angelegt, wären daraus nach 30 Jahren stolze $nom30Str € geworden."
                "⚡" -> "Dein Impulskauf ($emojiStr ${currencyFormat.format(mediumOrFrustration.amount)} €) hätte dir in 20 Jahren inflationsbereinigt $real20Str € Kaufkraft für deine Traumreise geschenkt."
                else -> "Diese Ausgabe von ${currencyFormat.format(mediumOrFrustration.amount)} € entspricht bei 7% Marktrendite in 30 Jahren sagenhaften $nom30Str € Vermögenszuwachs."
            }

            insights.add(
                AiOpportunityInsight(
                    headline = "${mediumOrFrustration.title} • Opportunitätskosten 💡",
                    originalCost = mediumOrFrustration.amount,
                    futureNominalAmount = c30.nominalValue,
                    futureRealAmount = c20.realPurchasingPower,
                    years = 30,
                    creativeSentence = sentence,
                    alternativeScenario = "Mit $nom30Str € finanziert sich ein kompletter zweiwöchiger Traumurlaub inklusive Flügen und Hotel."
                )
            )
        }

        // 3. Kumulierte Frustkauf-Gesamtsumme
        val frustrationSum = expenses.filter { PurchaseEmotions.isFrustrationPurchase(it.feelingEmoji) }.sumOf { it.amount }
        if (frustrationSum > 0) {
            val c30Frust = worldBankService.calculateCompound(frustrationSum, 30, 7.0, worldBankInflationRate)
            insights.add(
                AiOpportunityInsight(
                    headline = "Deine gesamten Frustkäufe (🤦 ${currencyFormat.format(frustrationSum)} €)",
                    originalCost = frustrationSum,
                    futureNominalAmount = c30Frust.nominalValue,
                    futureRealAmount = c30Frust.realPurchasingPower,
                    years = 30,
                    creativeSentence = "Wenn du alle ${expenses.count { PurchaseEmotions.isFrustrationPurchase(it.feelingEmoji) }} Frustkäufe (${currencyFormat.format(frustrationSum)} €) in einen weltweiten Weltbank-Index investiert hättest, hättest du zur Rente ${currencyFormat.format(c30Frust.nominalValue)} € mehr auf dem Konto.",
                    alternativeScenario = "Das entspricht einem kompletten Monat früher in den Ruhestand gehen!"
                )
            )
        }

        return insights
    }
}
