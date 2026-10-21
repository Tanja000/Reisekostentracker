package com.example.reisekostentracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reisekostentracker.model.AppLanguage
import com.example.reisekostentracker.model.AppStrings
import com.example.reisekostentracker.model.ExpenseCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCategoryDialog(
    initialCategory: ExpenseCategory?,
    language: AppLanguage = AppLanguage.DE,
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String, colorValue: Long) -> Unit
) {
    val isEn = language.isEnglish
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(initialCategory?.iconName ?: "local_activity") }
    var selectedColorValue by remember {
        mutableLongStateOf(initialCategory?.colorValue ?: ExpenseCategory.defaultColors.first())
    }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCategory == null) (if (isEn) "Create New Category" else "Neue Kategorie erstellen")
                else (if (isEn) "Edit Category" else "Kategorie bearbeiten"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(if (isEn) "Category Name" else "Kategoriename") },
                    placeholder = { Text(if (isEn) "e.g. Trips, Souvenirs, Beach..." else "z.B. Ausflüge, Souvenirs, Strand...") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(if (isEn) "Name cannot be empty" else "Name darf nicht leer sein") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Color Selection
                Text(
                    text = if (isEn) "Choose Color" else "Farbe wählen",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpenseCategory.defaultColors.forEach { colorVal ->
                        val isSelected = colorVal == selectedColorValue
                        val color = Color(colorVal)

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorValue = colorVal }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Icon Selection
                Text(
                    text = if (isEn) "Choose Icon" else "Symbol wählen",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.availableIcons.forEach { (iconKey, label) ->
                        val isSelected = iconKey == selectedIconName
                        val iconVector = ExpenseCategory.getIconByName(iconKey)

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(selectedColorValue).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIconName = iconKey }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, Color(selectedColorValue), RoundedCornerShape(12.dp))
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = label,
                                tint = if (isSelected) Color(selectedColorValue) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preview Badge
                Text(
                    text = if (isEn) "Preview" else "Vorschau",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))

                CategoryChip(
                    category = ExpenseCategory(
                        id = "preview",
                        name = if (name.isNotBlank()) name else (if (isEn) "Sample Category" else "Beispiel-Kategorie"),
                        iconName = selectedIconName,
                        colorValue = selectedColorValue
                    ),
                    isSelected = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(name.trim(), selectedIconName, selectedColorValue)
                    }
                }
            ) {
                Text(if (initialCategory == null) AppStrings.add(language) else AppStrings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancel(language))
            }
        }
    )
}
