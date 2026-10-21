package com.example.reisekostentracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reisekostentracker.model.PurchaseEmotions
import java.util.Locale

data class QuickAddPreset(
    val title: String,
    val defaultAmount: Double,
    val categoryId: String,
    val defaultPaymentMethod: String,
    val icon: ImageVector,
    val subtitle: String
)

object QuickAddPresets {
    val items = listOf(
        QuickAddPreset("Kaffee & Snack", 3.80, "cat_gastronomie", "Bar", Icons.Default.LocalCafe, "Café"),
        QuickAddPreset("Mittagessen", 12.50, "cat_gastronomie", "Karte", Icons.Default.Fastfood, "Lunch"),
        QuickAddPreset("Supermarkt", 35.00, "cat_lebensmittel", "Karte", Icons.Default.ShoppingCart, "Einkauf"),
        QuickAddPreset("Tanken / Ticket", 25.00, "cat_transport", "Karte", Icons.Default.DirectionsCar, "Transit")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddConfirmDialog(
    preset: QuickAddPreset,
    currentLocationName: String?,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String, location: String, feelingEmoji: String?) -> Unit
) {
    var amountText by remember {
        mutableStateOf(String.format(Locale.GERMAN, "%.2f", preset.defaultAmount))
    }
    var paymentMethod by remember { mutableStateOf(preset.defaultPaymentMethod) }
    var feelingEmoji by remember { mutableStateOf<String?>(null) }
    var locationText by remember {
        mutableStateOf(
            if (!currentLocationName.isNullOrBlank() && !currentLocationName.contains("fehlt") && !currentLocationName.contains("ermittelt")) {
                currentLocationName
            } else {
                "Karlsruhe"
            }
        )
    }
    var isError by remember { mutableStateOf(false) }

    val quickCities = remember {
        listOf("Karlsruhe", "München", "Berlin", "Hamburg", "Köln", "Frankfurt", "Stuttgart", "Wien", "Zürich")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        title = {
            Text(
                text = preset.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Betrag & Ort anpassen und verbuchen:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Betrag Feld
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Betrag in €") },
                    leadingIcon = { Icon(Icons.Default.Euro, contentDescription = null) },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Gültigen Betrag eingeben (z.B. 4,20)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Ort Feld (anpassbar mit Geocoding)
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Ort / Stadt (wird auf Karte platziert)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Schnellauswahl beliebter Orte
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!currentLocationName.isNullOrBlank() && !currentLocationName.contains("fehlt") && !currentLocationName.contains("ermittelt")) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { locationText = currentLocationName }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mein GPS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    items(quickCities) { city ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (locationText.equals(city, ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { locationText = city }
                        ) {
                            Text(
                                text = city,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (locationText.equals(city, ignoreCase = true)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 10 Emojis für Kaufempfindungen (sofern nicht Grundnahrungsmittel)
                if (PurchaseEmotions.shouldAskEmotion(preset.categoryId)) {
                    Text(
                        text = "Wie hast du diesen Kauf empfunden?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PurchaseEmotions.list) { emotion ->
                            val isSelected = feelingEmoji == emotion.emoji
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.clickable {
                                    feelingEmoji = if (feelingEmoji == emotion.emoji) null else emotion.emoji
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = emotion.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = emotion.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Zahlungsart
                Text(
                    text = "Zahlungsmethode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Karte", "Bar", "Online").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.replace(",", ".").toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) {
                        val loc = locationText.ifBlank { "Karlsruhe" }
                        onConfirm(parsed, paymentMethod, loc, feelingEmoji)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Buchen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
