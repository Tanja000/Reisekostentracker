package com.example.reisekostentracker.model

data class PurchaseEmotion(
    val emoji: String,
    val title: String,
    val description: String,
    val isFrustrationOrImpulse: Boolean = false,
    val sentimentWeight: Int // 2 (Liebe) bis -3 (Reue/Ärger)
)

object PurchaseEmotions {
    /**
     * Die 10 Emojis für Konsumempfindungen nach dem Kauf:
     * Von absoluter Begeisterung über Routine bis hin zu Impuls- und Frustkäufen.
     */
    val list: List<PurchaseEmotion> = listOf(
        PurchaseEmotion(
            emoji = "😍",
            title = "Traumkauf",
            description = "Liebe es / Absolutes Highlight",
            isFrustrationOrImpulse = false,
            sentimentWeight = 2
        ),
        PurchaseEmotion(
            emoji = "😊",
            title = "Zufrieden",
            description = "Sinnvoll & gutes Gefühl",
            isFrustrationOrImpulse = false,
            sentimentWeight = 1
        ),
        PurchaseEmotion(
            emoji = "🥳",
            title = "Belohnung",
            description = "Gönnung / Feier des Tages",
            isFrustrationOrImpulse = false,
            sentimentWeight = 1
        ),
        PurchaseEmotion(
            emoji = "☕",
            title = "Routine",
            description = "Alltag & bewusste Gewohnheit",
            isFrustrationOrImpulse = false,
            sentimentWeight = 0
        ),
        PurchaseEmotion(
            emoji = "🤔",
            title = "Zweifel",
            description = "War es das wirklich wert?",
            isFrustrationOrImpulse = false,
            sentimentWeight = -1
        ),
        PurchaseEmotion(
            emoji = "⚡",
            title = "Impulskauf",
            description = "Spontan zugegriffen ohne Plan",
            isFrustrationOrImpulse = true,
            sentimentWeight = -1
        ),
        PurchaseEmotion(
            emoji = "🥱",
            title = "Langeweile",
            description = "Gekauft aus Gewohnheit/Trägheit",
            isFrustrationOrImpulse = true,
            sentimentWeight = -2
        ),
        PurchaseEmotion(
            emoji = "💸",
            title = "Zu teuer",
            description = "Zähneknirschend gezahlt",
            isFrustrationOrImpulse = true,
            sentimentWeight = -2
        ),
        PurchaseEmotion(
            emoji = "🤦",
            title = "Frustkauf",
            description = "Reue / Emotionaler Fehltritt",
            isFrustrationOrImpulse = true,
            sentimentWeight = -3
        ),
        PurchaseEmotion(
            emoji = "😡",
            title = "Ärger",
            description = "Totaler Fehlkauf / Verärgerung",
            isFrustrationOrImpulse = true,
            sentimentWeight = -3
        )
    )

    fun fromEmoji(emoji: String?): PurchaseEmotion? = list.find { it.emoji == emoji }

    /**
     * Prüft, ob ein Kauf emotional bewertet werden soll:
     * Bei Mietenzahlung (Wohnen) und Grundnahrungsmitteln (Lebensmittel/Supermarkt)
     * entfällt die Emoji-Bewertung, da es sich um fundamentale Fixkosten handelt.
     */
    fun shouldAskEmotion(categoryId: String?): Boolean {
        if (categoryId == null) return true
        val cat = categoryId.lowercase()
        return cat != "cat_wohnen" &&
               cat != "cat_lebensmittel" &&
               !cat.contains("wohnen") &&
               !cat.contains("miete") &&
               !cat.contains("lebensmittel")
    }

    /**
     * Prüft, ob ein bestimmtes Emoji als Frust- oder Reuekauf einzustufen ist.
     */
    fun isFrustrationPurchase(emoji: String?): Boolean {
        if (emoji == null) return false
        val found = fromEmoji(emoji)
        return found?.isFrustrationOrImpulse == true || emoji in listOf("🤦", "😡", "💸", "🥱", "⚡")
    }
}
