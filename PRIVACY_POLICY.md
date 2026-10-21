# Datenschutzerklärung (Privacy Policy) – SaveIQ

**Stand:** 21. Oktober 2026  
**Gültig für:** SaveIQ Android App  
**Verantwortlicher:** Entwickler von SaveIQ (`de.saveiq.app`)  
**Kontakt:** support@saveiq.app (bzw. Kontaktadresse des Entwicklers im Google Play Store)

---

## 1. Grundsatz: Lokale Datenspeicherung & Privatsphäre

Die Privatsphäre und der Schutz Ihrer persönlichen Daten stehen bei **SaveIQ** an erster Stelle. SaveIQ wurde nach dem Prinzip der **Datensparsamkeit („Privacy by Design“)** entwickelt:

- **Alle Ausgabeneinträge, Beträge, Kategorien, Notizen, Emojis, Budgets und historischen Reisedaten werden ausschließlich lokal auf Ihrem Endgerät in einer SQLite-Datenbank gespeichert.**
- Es gibt **keine Benutzerregistrierung**, **keinen Cloud-Account-Zwang** und **keine Übertragung** Ihrer persönlichen Finanz- und Buchungsdaten an Server des App-Entwicklers oder an unbeteiligte Dritte.
- Die App enthält **keine Werbe-Netzwerke**, **keine Tracking-Tools** (wie z. B. Google Analytics, Firebase Analytics, Meta Pixel) und verkauft niemals Daten.

---

## 2. Erforderliche App-Berechtigungen & Zweck

### 2.1 Standortberechtigung (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- **Zweck:** Wenn Sie eine neue Ausgabe eintragen, kann SaveIQ den aktuellen Ort (z. B. Stadtname) automatisch ermitteln und mit dem Eintrag verknüpfen, um Ihnen im Reiter *„Karte“* und in den Statistiken Ihre Ausgaben geographisch anzuzeigen.
- **Verarbeitung:** Die Ermittlung erfolgt über den Android-Geocoding-Dienst direkt auf Ihrem Gerät. Die Geokoordinaten und Ortsnamen verbleiben auf Ihrem Endgerät und werden zu keinem Zeitpunkt an externe Server des Betreibers übermittelt.
- **Freiwilligkeit:** Die Freigabe des Standorts ist vollkommen freiwillig. Sie können die Berechtigung verweigern oder in den Android-Systemeinstellungen jederzeit widerrufen. Ausgaben und Orte können alternativ auch manuell eingegeben werden.

### 2.2 Internetzugriff (`INTERNET`)
Die Internetberechtigung wird ausschließlich für folgende zwei klar definierte, unpersonalisierte Funktionen genutzt:

1. **Kartenansicht (OpenFreeMap / OpenStreetMap):**
   - Zum Laden der Vektorkarten-Kacheln kontaktiert die App den freien Dienst `tiles.openfreemap.org`.
   - Hierbei werden technisch notwendige Metadaten (z. B. IP-Adresse zur Auslieferung der Kacheln durch den Kartenanbieter) übertragen, jedoch **keine personenbezogenen Daten, keine Identifikationsmerkmale und keine Ausgabeninhalte**.

2. **Inflations- und Marktdaten (Weltbank Open Data API):**
   - Zum Erfassen aktueller makroökonomischer Inflationsraten kontaktiert die App die offizielle, öffentliche Weltbank-Schnittstelle (`api.worldbank.org`).
   - Auch hierbei werden **keine persönlichen Nutzerdaten** übertragen.

---

## 3. Lokale Künstliche Intelligenz / Android AICore

Alle Vorschläge, Spartipps und Frustkauf-Analysen im Reiter *„Spar-KI“* laufen entweder über die lokale Android-System-Schnittstelle (**Android AICore on-device**) oder über interne Algorithmen auf Ihrem Smartphone. Es werden **keine Ausgabeninhalte an externe KI-Cloud-Dienste übertragen**.

---

## 4. Speicherdauer & Löschung von Daten

- Ihre Daten bleiben so lange auf Ihrem Gerät gespeichert, wie die App installiert ist.
- **Vollständiges Löschen:** Sie können jederzeit über das Menü der App (*„Alle Ausgaben löschen“*) oder über das Löschen des App-Speichers in den Android-Einstellungen alle Daten unwiderruflich von Ihrem Gerät entfernen.
- Beim Deinstallieren der App werden alle lokal gespeicherten Daten durch das Betriebssystem Android restlos gelöscht.

---

## 5. Ihre Rechte gemäß DSGVO

Sofern Sie sich in der Europäischen Union befinden, stehen Ihnen gemäß der Datenschutz-Grundverordnung (DSGVO) folgende Rechte zu:
- Recht auf Auskunft (Art. 15 DSGVO)
- Recht auf Berichtigung (Art. 16 DSGVO)
- Recht auf Löschung („Recht auf Vergessenwerden“, Art. 17 DSGVO)
- Recht auf Einschränkung der Verarbeitung (Art. 18 DSGVO)
- Recht auf Datenübertragbarkeit (Art. 20 DSGVO)

Da SaveIQ keinerlei personenbezogene Daten auf externen Servern speichert oder sammelt, haben Sie zu jedem Zeitpunkt die **vollständige, unmittelbare Kontrolle über Ihre Daten direkt in der App auf Ihrem Smartphone**.

---

## 6. Haftungsausschluss zu Finanz- und Sparberechnungen

Alle in der App dargestellten Zinseszins-, Sparpotenzial- und Opportunitätsrechnungen (insbesondere Hochrechnungen mit 7 % p.a. ETF-Durchschnittsrendite) dienen ausschließlich zu Illustrations- und Bildungszwecken. Sie stellen keine Anlageberatung, Steuerberatung oder Finanzdienstleistung dar. Historische Renditen sind keine Garantie für zukünftige Entwicklungen.

---

## 7. Open-Source-Lizenzen & Attribution

- **Kartenmaterial:** © [OpenStreetMap](https://www.openstreetmap.org/copyright)-Mitwirkende, bereitgestellt über [OpenFreeMap](https://openfreemap.org/).
- **Inflationsdaten:** Bereitgestellt durch die [World Bank Open Data API](https://data.worldbank.org/) unter Creative Commons Attribution 4.0 (CC BY 4.0).
- **MapLibre GL Android SDK:** Lizenziert unter der BSD-2-Clause Lizenz.
- **AndroidX & Jetpack Compose:** Lizenziert unter Apache License 2.0.
