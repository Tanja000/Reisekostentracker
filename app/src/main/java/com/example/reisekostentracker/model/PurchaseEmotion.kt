package com.example.reisekostentracker.model

data class PurchaseEmotion(
    val emoji: String,
    val title: String,
    val description: String,
    val isFrustrationOrImpulse: Boolean = false,
    val sentimentWeight: Int, // 2 (Liebe) bis -3 (Reue/Ärger)
    val titleEn: String = title,
    val descriptionEn: String = description
) {
    fun localizedTitle(isEn: Boolean) = if (isEn) titleEn else title
    fun localizedDescription(isEn: Boolean) = if (isEn) descriptionEn else description
}

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
            sentimentWeight = 2,
            titleEn = "Dream Buy",
            descriptionEn = "Love it / Absolute highlight"
        ),
        PurchaseEmotion(
            emoji = "😊",
            title = "Zufrieden",
            description = "Sinnvoll & gutes Gefühl",
            isFrustrationOrImpulse = false,
            sentimentWeight = 1,
            titleEn = "Satisfied",
            descriptionEn = "Worthwhile & feels great"
        ),
        PurchaseEmotion(
            emoji = "🥳",
            title = "Belohnung",
            description = "Gönnung / Feier des Tages",
            isFrustrationOrImpulse = false,
            sentimentWeight = 1,
            titleEn = "Reward",
            descriptionEn = "Treat / Celebrating the day"
        ),
        PurchaseEmotion(
            emoji = "☕",
            title = "Routine",
            description = "Alltag & bewusste Gewohnheit",
            isFrustrationOrImpulse = false,
            sentimentWeight = 0,
            titleEn = "Routine",
            descriptionEn = "Daily habit & conscious routine"
        ),
        PurchaseEmotion(
            emoji = "🤔",
            title = "Zweifel",
            description = "War es das wirklich wert?",
            isFrustrationOrImpulse = false,
            sentimentWeight = -1,
            titleEn = "Doubt",
            descriptionEn = "Was it really worth it?"
        ),
        PurchaseEmotion(
            emoji = "⚡",
            title = "Impulskauf",
            description = "Spontan zugegriffen ohne Plan",
            isFrustrationOrImpulse = true,
            sentimentWeight = -1,
            titleEn = "Impulse Buy",
            descriptionEn = "Grabbed spontaneously without plan"
        ),
        PurchaseEmotion(
            emoji = "🥱",
            title = "Langeweile",
            description = "Gekauft aus Gewohnheit/Trägheit",
            isFrustrationOrImpulse = true,
            sentimentWeight = -2,
            titleEn = "Boredom",
            descriptionEn = "Bought out of boredom/habit"
        ),
        PurchaseEmotion(
            emoji = "💸",
            title = "Zu teuer",
            description = "Zähneknirschend gezahlt",
            isFrustrationOrImpulse = true,
            sentimentWeight = -2,
            titleEn = "Overpriced",
            descriptionEn = "Paid through gritted teeth"
        ),
        PurchaseEmotion(
            emoji = "🤦",
            title = "Frustkauf",
            description = "Reue / Emotionaler Fehltritt",
            isFrustrationOrImpulse = true,
            sentimentWeight = -3,
            titleEn = "Regret Buy",
            descriptionEn = "Regret / Emotional misstep"
        ),
        PurchaseEmotion(
            emoji = "😡",
            title = "Ärger",
            description = "Totaler Fehlkauf / Verärgerung",
            isFrustrationOrImpulse = true,
            sentimentWeight = -3,
            titleEn = "Anger",
            descriptionEn = "Total waste / Deep frustration"
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
               cat != "wohnen" &&
               cat != "lebensmittel" &&
               cat != "miete" &&
               !cat.contains("supermarkt") &&
               !cat.contains("groceries") &&
               !cat.contains("housing")
    }

    fun isFrustrationPurchase(emoji: String?): Boolean {
        val emotion = fromEmoji(emoji) ?: return false
        return emotion.isFrustrationOrImpulse
    }
}
