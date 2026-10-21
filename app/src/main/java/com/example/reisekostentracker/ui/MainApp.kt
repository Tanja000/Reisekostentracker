package com.example.reisekostentracker.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reisekostentracker.ui.components.AddEditCategoryDialog
import com.example.reisekostentracker.ui.components.AddEditExpenseDialog
import com.example.reisekostentracker.ui.screens.AiSavingsScreen
import com.example.reisekostentracker.ui.screens.BudgetsScreen
import com.example.reisekostentracker.ui.screens.CategoriesScreen
import com.example.reisekostentracker.ui.screens.ExpensesScreen
import com.example.reisekostentracker.ui.screens.MapScreen
import com.example.reisekostentracker.ui.screens.StatisticsScreen
import com.example.reisekostentracker.ui.viewmodel.ExpenseViewModel

sealed class NavItem(val title: String, val icon: ImageVector) {
    object Expenses : NavItem("Ausgaben", Icons.Default.Payments)
    object Statistics : NavItem("Statistiken", Icons.Default.Analytics)
    object SavingsAi : NavItem("Spar-KI", Icons.Default.AutoAwesome)
    object Map : NavItem("Karte", Icons.Default.Map)
    object Categories : NavItem("Kategorien", Icons.Default.Category)
    object Budgets : NavItem("Budgets", Icons.Default.AccountBalanceWallet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isLocationConsentDialogOpen by viewModel.isLocationConsentDialogOpen.collectAsState()
    val hasLocationConsent by viewModel.hasLocationConsent.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val granted = fine || coarse
        viewModel.setLocationConsent(granted)
        if (granted) {
            viewModel.fetchCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineCheck && !coarseCheck) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.setLocationConsent(true)
            viewModel.fetchCurrentLocation()
        }
    }

    val navItems = listOf(
        NavItem.Expenses,
        NavItem.Statistics,
        NavItem.SavingsAi,
        NavItem.Map,
        NavItem.Categories,
        NavItem.Budgets
    )

    val isExpenseDialogOpen by viewModel.isExpenseDialogOpen.collectAsState()
    val editingExpense by viewModel.editingExpense.collectAsState()

    val isCategoryDialogOpen by viewModel.isCategoryDialogOpen.collectAsState()
    val editingCategory by viewModel.editingCategory.collectAsState()

    val categories by viewModel.categories.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TrackMyCent",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // GPS Location Refresh Button
                    IconButton(onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS Standort",
                            tint = if (hasLocationConsent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Dark Mode Toggle Button
                    IconButton(onClick = { viewModel.toggleDarkTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            tint = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Demo-Finanzdaten laden") },
                            onClick = {
                                viewModel.seedDemoData()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Alle Ausgaben löschen") },
                            onClick = {
                                viewModel.clearAllData()
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    0 -> ExpensesScreen(viewModel = viewModel)
                    1 -> StatisticsScreen(viewModel = viewModel)
                    2 -> AiSavingsScreen(viewModel = viewModel)
                    3 -> MapScreen(viewModel = viewModel)
                    4 -> CategoriesScreen(viewModel = viewModel)
                    5 -> BudgetsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // GPS Location Consent Dialog
    if (isLocationConsentDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setLocationConsent(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(text = "GPS-Standort erfassen?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Möchtest du, dass TrackMyCent beim Eintragen von Ausgaben automatisch deinen GPS-Standort erfasst?\n\nSo siehst du auf der Ausgaben-Karte direkt, wo du warst und wo du wie viel Geld bezahlt hast."
                )
            },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Standort erlauben")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setLocationConsent(false) }) {
                    Text("Nicht jetzt")
                }
            }
        )
    }

    // Dialogs
    if (isExpenseDialogOpen) {
        val lastLocationName by viewModel.lastLocationName.collectAsState()
        AddEditExpenseDialog(
            initialExpense = editingExpense,
            categories = categories,
            currentLocationName = lastLocationName,
            onDismiss = { viewModel.closeExpenseDialog() },
            onSave = { title, amount, currency, categoryId, tripId, date, notes, paymentMethod, locationName, feelingEmoji ->
                viewModel.saveExpense(
                    title, amount, currency, categoryId, tripId, date, notes, paymentMethod,
                    locationName = locationName,
                    feelingEmoji = feelingEmoji
                )
            }
        )
    }

    if (isCategoryDialogOpen) {
        AddEditCategoryDialog(
            initialCategory = editingCategory,
            onDismiss = { viewModel.closeCategoryDialog() },
            onSave = { name, iconName, colorValue ->
                viewModel.saveCategory(name, iconName, colorValue)
            }
        )
    }
}
