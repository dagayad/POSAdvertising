package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NavigationTab
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.screens.AddEditCustomerDialog
import com.example.ui.screens.AddEditServiceDialog
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.CreateInvoiceSheet
import com.example.ui.screens.CustomerStatementDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.ManageCategoriesDialog
import com.example.ui.screens.RecordPaymentDialog
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectStudioApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val services by viewModel.services.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Filters & Searches
    val invoiceSearch by viewModel.invoiceSearch.collectAsState()
    val invoiceStatusFilter by viewModel.invoiceStatusFilter.collectAsState()
    val serviceSearch by viewModel.serviceSearch.collectAsState()
    val serviceCategoryFilter by viewModel.serviceCategoryFilter.collectAsState()
    val customerSearch by viewModel.customerSearch.collectAsState()
    val expenseSearch by viewModel.expenseSearch.collectAsState()

    // Dialog states
    val isCreateInvoiceOpen by viewModel.isCreateInvoiceOpen.collectAsState()
    val selectedInvoiceForReceipt by viewModel.selectedInvoiceForReceipt.collectAsState()
    val selectedInvoiceForPayment by viewModel.selectedInvoiceForPayment.collectAsState()
    val selectedCustomerForStatement by viewModel.selectedCustomerForStatement.collectAsState()
    val isAddCustomerOpen by viewModel.isAddCustomerOpen.collectAsState()
    val editingCustomer by viewModel.editingCustomer.collectAsState()
    val isAddServiceOpen by viewModel.isAddServiceOpen.collectAsState()
    val editingService by viewModel.editingService.collectAsState()
    val isManageCategoriesOpen by viewModel.isManageCategoriesOpen.collectAsState()
    val isAddExpenseOpen by viewModel.isAddExpenseOpen.collectAsState()

    // BackHandler to return to Dashboard tab
    BackHandler(enabled = currentTab != NavigationTab.OVERVIEW) {
        viewModel.setTab(NavigationTab.OVERVIEW)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(listOf(BrandIndigo, BrandCyan))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Con-nect Studio",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "Advertising Business OS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                val navItems = listOf(
                    Triple(NavigationTab.OVERVIEW, NavigationTab.OVERVIEW.label, Icons.Default.Dashboard),
                    Triple(NavigationTab.INVOICES, NavigationTab.INVOICES.label, Icons.Default.Receipt),
                    Triple(NavigationTab.SERVICES, NavigationTab.SERVICES.label, Icons.Default.Category),
                    Triple(NavigationTab.CUSTOMERS, NavigationTab.CUSTOMERS.label, Icons.Default.People),
                    Triple(NavigationTab.EXPENSES, NavigationTab.EXPENSES.label, Icons.Default.MoneyOff),
                    Triple(NavigationTab.SETTINGS, NavigationTab.SETTINGS.label, Icons.Default.Settings)
                )

                navItems.forEach { (tab, label, icon) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
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
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    NavigationTab.OVERVIEW -> DashboardScreen(
                        metrics = metrics,
                        settings = settings,
                        recentInvoices = invoices,
                        onNavigateTab = { viewModel.setTab(it) },
                        onOpenCreateInvoice = { viewModel.setCreateInvoiceOpen(true) },
                        onOpenAddService = { viewModel.openAddService(null) },
                        onOpenAddExpense = { viewModel.setAddExpenseOpen(true) },
                        onOpenReceipt = { viewModel.openReceiptForInvoice(it) },
                        onOpenPayment = { viewModel.openPaymentForInvoice(it) }
                    )

                    NavigationTab.INVOICES -> InvoicesScreen(
                        invoices = invoices,
                        customers = customers,
                        services = services,
                        settings = settings,
                        searchQuery = invoiceSearch,
                        onSearchChange = { viewModel.setInvoiceSearch(it) },
                        statusFilter = invoiceStatusFilter,
                        onStatusFilterChange = { viewModel.setInvoiceStatusFilter(it) },
                        onOpenCreateInvoice = { viewModel.setCreateInvoiceOpen(true) },
                        onOpenReceipt = { viewModel.openReceiptForInvoice(it) },
                        onOpenPayment = { viewModel.openPaymentForInvoice(it) },
                        onDeleteInvoice = { viewModel.deleteInvoice(it) }
                    )

                    NavigationTab.SERVICES -> ServicesScreen(
                        services = services,
                        categories = categories,
                        settings = settings,
                        searchQuery = serviceSearch,
                        onSearchChange = { viewModel.setServiceSearch(it) },
                        categoryFilter = serviceCategoryFilter,
                        onCategoryFilterChange = { viewModel.setServiceCategoryFilter(it) },
                        onOpenAddService = { viewModel.openAddService(it) },
                        onDeleteService = { viewModel.deleteService(it) },
                        onOpenManageCategories = { viewModel.setManageCategoriesOpen(true) }
                    )

                    NavigationTab.CUSTOMERS -> CustomersScreen(
                        customers = customers,
                        invoices = invoices,
                        settings = settings,
                        searchQuery = customerSearch,
                        onSearchChange = { viewModel.setCustomerSearch(it) },
                        onOpenAddCustomer = { viewModel.openAddCustomer(it) },
                        onDeleteCustomer = { viewModel.deleteCustomer(it) },
                        onOpenStatement = { viewModel.openCustomerStatement(it) },
                        onOpenPayment = { viewModel.openPaymentForInvoice(it) }
                    )

                    NavigationTab.EXPENSES -> ExpensesScreen(
                        expenses = expenses,
                        settings = settings,
                        searchQuery = expenseSearch,
                        onSearchChange = { viewModel.setExpenseSearch(it) },
                        onOpenAddExpense = { viewModel.setAddExpenseOpen(true) },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )

                    NavigationTab.SETTINGS -> SettingsScreen(
                        settings = settings,
                        onSaveSettings = { viewModel.updateSettings(it) },
                        onClearTransactions = { viewModel.clearTransactions() }
                    )
                }
            }
        }
    }

    // Modal & Dialog Overlays
    if (isCreateInvoiceOpen) {
        CreateInvoiceSheet(
            customers = customers,
            services = services,
            settings = settings,
            onDismiss = { viewModel.setCreateInvoiceOpen(false) },
            onSubmit = { newInvoice ->
                viewModel.createInvoice(newInvoice) { created ->
                    viewModel.openReceiptForInvoice(created)
                }
            }
        )
    }

    selectedInvoiceForReceipt?.let { inv ->
        val invPayments = payments.filter { it.invoiceId == inv.id }
        ThermalReceiptDialog(
            invoice = inv,
            payments = invPayments,
            settings = settings,
            onDismiss = { viewModel.openReceiptForInvoice(null) }
        )
    }

    selectedInvoiceForPayment?.let { inv ->
        RecordPaymentDialog(
            invoice = inv,
            currency = settings.currency,
            onDismiss = { viewModel.openPaymentForInvoice(null) },
            onRecord = { amount, method, date, ref, note ->
                viewModel.recordPayment(inv, amount, method, date, ref, note)
            }
        )
    }

    selectedCustomerForStatement?.let { customer ->
        val customerInvoices = invoices.filter { it.customerId == customer.id }
        val customerPayments = payments.filter { p -> customerInvoices.any { it.id == p.invoiceId } }
        CustomerStatementDialog(
            customer = customer,
            invoices = customerInvoices,
            payments = customerPayments,
            settings = settings,
            onDismiss = { viewModel.openCustomerStatement(null) }
        )
    }

    if (isAddCustomerOpen) {
        AddEditCustomerDialog(
            customer = editingCustomer,
            onDismiss = { viewModel.closeAddCustomer() },
            onSave = { viewModel.saveCustomer(it) }
        )
    }

    if (isAddServiceOpen) {
        AddEditServiceDialog(
            service = editingService,
            categories = categories,
            currency = settings.currency,
            onDismiss = { viewModel.closeAddService() },
            onSave = { viewModel.saveService(it) }
        )
    }

    if (isManageCategoriesOpen) {
        ManageCategoriesDialog(
            categories = categories,
            onDismiss = { viewModel.setManageCategoriesOpen(false) },
            onAddCategory = { viewModel.addCategory(it) },
            onDeleteCategory = { viewModel.deleteCategory(it) }
        )
    }

    if (isAddExpenseOpen) {
        AddExpenseDialog(
            currency = settings.currency,
            onDismiss = { viewModel.setAddExpenseOpen(false) },
            onSave = { viewModel.saveExpense(it) }
        )
    }
}
